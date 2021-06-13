import warnings
warnings.filterwarnings('ignore')
import sys 
import os
import argparse
import shutil
import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
import matplotlib.dates as mdates
#%matplotlib inline
import seaborn as sns
from statsmodels.tsa.seasonal import seasonal_decompose
from sklearn.preprocessing import StandardScaler
from sklearn.covariance import EllipticEnvelope
from sklearn.metrics import accuracy_score, confusion_matrix, f1_score, recall_score

####################################

parser = argparse.ArgumentParser(description='Find anomalies in wearables time-series data.')
parser.add_argument('--myphd_id',metavar='', default = 'myphd_id', help ='user myphd_id')
parser.add_argument('--symptom_date', metavar='', default = 'NaN', help = 'symptom date with y-m-d format')
parser.add_argument('--diagnosis_date', metavar='', default = 'NaN',  help='diagnosis date with y-m-d format')
args = parser.parse_args()

myphd_id = args.myphd_id
symptom_date = args.symptom_date
diagnosis_date = args.diagnosis_date

class HROSAD_offline:
    #필터링 단계
    def HROS(self, heartrate, steps):
        # HROS은 심박수 및 걸음수 데이터를 사용하여 안정시 심박수를 추론합니다. 

        # 심박수
        df_hr = pd.read_csv(hr_data)
        df_hr = df_hr.set_index('datetime')
        df_hr.index.name = None
        df_hr.index = pd.to_datetime(df_hr.index)

        # 걸음수
        df_steps = pd.read_csv(steps_data)
        df_steps = df_steps.set_index('datetime')
        df_steps.index.name = None
        df_steps.index = pd.to_datetime(df_steps.index)
        df_steps["steps"] = df_steps["steps"].apply(lambda x: x + 1) # 0분활 문제를 박기위해 모든 걸음 수에 +1를 합니다.

        df1 = pd.merge(df_hr, df_steps, left_index=True, right_index=True) #같은 날,시간,분,초 심박수와 걸음수끼리 csv 병합
        df1['heartrate'] = (df1['heartrate']/df1['steps']) # 안정시 심박수 = 심박수/걸음수
        return df1

    # 전처리 단계
    def pre_processing(self, resting_heart_rate):
        # 안정시 심박수데이터를 이동평균(moving averages) 400 
        # 다운 샘플링을 1시간으로 하여 데이터를 평활화합니다.

        df_nonas = df1.dropna()
        df1_rom = df_nonas.rolling(400).mean() #이동 평균

        df2 = df1_rom.resample('1H').mean() # 다운 샘플링
        df2 = df2.dropna()
        return df2

    # 주기성 보정 단계
    def seasonality_correction(self, heartrate, steps):
        # 데이터를 사전 처리하고 계절성을 보정합니다.

        # 심박수, 걸음수 데이터를 additive모델을 사용하여 trend + resid하여 나누고 주기를 1로 주어 분석을 합니다.
        sdHR_decomposition = seasonal_decompose(sdHR, model='additive', freq=1)
        sdSteps_decomposition = seasonal_decompose(sdSteps, model='additive', freq=1)
        sdHR_decomp = pd.DataFrame(sdHR_decomposition.resid + sdHR_decomposition.trend)
        sdHR_decomp.rename(columns={sdHR_decomp.columns[0]:'heartrate'}, inplace=True)
        sdSteps_decomp = pd.DataFrame(sdSteps_decomposition.resid + sdSteps_decomposition.trend)
        sdSteps_decomp.rename(columns={sdSteps_decomp.columns[0]:'steps_window_12'}, inplace=True)
        frames = [sdHR_decomp, sdSteps_decomp]
        data = pd.concat(frames, axis=1)
        return data

    # 표준화 단계
    def standardization(self, seasonality_corrected_data):
        # 가우시안 분포 평균값이 0, 분산을 1로 단위분산(Z점수)으로 데이터를 표준화 합니다. 
        data_scaled = StandardScaler().fit_transform(data_seasnCorec.values)
        data_scaled_features = pd.DataFrame(data_scaled, index=data_seasnCorec.index, columns=data_seasnCorec.columns)
        data_df = pd.DataFrame(data_scaled_features)
        data = pd.DataFrame(data_df).fillna(0)
        return data

    # 모델 교육 및 이상탐지 예측
    def anomaly_detection(self, standardized_data):
        # 표준화된 데이터를 사용하여 Mahalanobis_MCD을 사용하여 특이치를 탐지 합니다.
        # 분석된 데이터는 1은 정상 -1은 비정상으로 분류합니다.

        # Mahalanobis_MCD를 사용하여 기존모델에 MCD 최소공분산추정을 값을 설정해 줍니다. (support_fraction=0.7)
        # .fit을 하여 데이터를 학습합니다.
        model = EllipticEnvelope(contamination=0.1, random_seed=10, support_fraction=0.7)
        model.fit(std_data)

        # 결과를 csv파일에 컬럼이름 변경 후 저장
        preds = pd.DataFrame(model.predict(std_data))
        preds = preds.rename(lambda x: 'anomaly' if x == 0 else x, axis=1)
        data = std_data.reset_index()
        data = data.join(preds)
        return data

    # 시각화
    def visualize(self, results, symptom_date, diagnosis_date, user):
        #결과를 csv파일, PNG파일로 저장
        try:

            with plt.style.context('fivethirtyeight'):
                fig, ax = plt.subplots(1, figsize=(80,15))
                a = data.loc[data['anomaly'] == -1, ('index', 'heartrate')] 
                b = a[(a['heartrate']> 0)]
                ax.bar(data['index'], data['heartrate'], linestyle='-',color='midnightblue' ,lw=6, width=0.01)
                ax.scatter(b['index'],b['heartrate'], color='red', label='Anomaly', s=500)
                ax.tick_params(axis='both', which='major', color='blue', labelsize=60)
                ax.tick_params(axis='both', which='minor', color='blue', labelsize=60)
                ax.set_title("MiT_anomaly_detection",fontweight="bold", size=50) 
                ax.set_ylabel('Std. HROS\n', fontsize = 50) 
                ax.axvline(pd.to_datetime(symptom_date), color='red', zorder=1, linestyle='--', lw=8)  
                ax.axvline(pd.to_datetime(diagnosis_date), color='purple',zorder=1, linestyle='--', lw=8) 
                ax.tick_params(axis='both', which='major', labelsize=60)
                ax.tick_params(axis='both', which='minor', labelsize=60)
                ax.xaxis.set_major_locator(mdates.DayLocator(interval=7))
                ax.grid(zorder=0)
                ax.grid(True)
                plt.xticks(fontsize=30, rotation=90)
                plt.yticks(fontsize=50)
                ax.patch.set_facecolor('white')
                fig.patch.set_facecolor('white')
                plt.show()
                figure = fig.savefig(f"{user}.png", bbox_inches='tight')  
                # Anomaly results
                b['Anomalies'] = myphd_id
                b.to_csv(myphd_id_anomalies, mode='a', header=False)        
                return figure

        except:
            with plt.style.context('fivethirtyeight'):
                fig, ax = plt.subplots(1, figsize=(80,15))
                a = data.loc[data['anomaly'] == -1, ('index', 'heartrate')] 
                b = a[(a['heartrate']> 0)]
                ax.bar(data['index'], data['heartrate'], linestyle='-',color='midnightblue' ,lw=6, width=0.01)
                ax.scatter(b['index'],b['heartrate'], color='red', label='Anomaly', s=1000)
                ax.tick_params(axis='both', which='major', color='blue', labelsize=60)
                ax.tick_params(axis='both', which='minor', color='blue', labelsize=60)
                ax.set_title("MiT_anomaly_detection",fontweight="bold", size=50) 
                ax.set_ylabel('Std. HROS\n', fontsize = 50) 
                ax.tick_params(axis='both', which='major', labelsize=60)
                ax.tick_params(axis='both', which='minor', labelsize=60)
                ax.xaxis.set_major_locator(mdates.DayLocator(interval=7))
                ax.grid(zorder=0)
                ax.grid(True)
                plt.xticks(fontsize=30, rotation=90)
                plt.yticks(fontsize=50)
                ax.patch.set_facecolor('white')
                fig.patch.set_facecolor('white')     
                figure = fig.savefig(f"{user}.png", bbox_inches='tight')  
                b['Anomalies'] = myphd_id
                b.to_csv(myphd_id_anomalies, mode='a', header=False)        
                return figure

entries = os.listdir('C:/Users/whgud/바탕 화면/python_workspace/COVID-19-Wearables')

import tqdm

users = []

# 사용자 파일 이름을 전부 가져와 각각 id를 추출후 리스트에 append
for entry in entries:
    user = entry[0:7]
    if not user in users:
        users.append(user)

err_count = 0 #에러카운더



# 리스트의 사용자 ID를 반복문을 사용하여 분석
for user in tqdm.tqdm(users):

    try:
        model = HROSAD_offline()
        hr_data = f'C:/Users/whgud/바탕 화면/python_workspace/COVID-19-Wearables/{user}_hr.csv'
        steps_data = f'C:/Users/whgud/바탕 화면/python_workspace/COVID-19-Wearables/{user}_steps.csv'
        df1 = model.HROS(hr_data,steps_data)
        df2 = model.pre_processing(df1)

        sdHR = df2[['heartrate']]
        sdSteps = df2[['steps']]
        data_seasnCorec = model.seasonality_correction(sdHR, sdSteps)
        data_seasnCorec += 0.1
        std_data = model.standardization(data_seasnCorec)
        data = model.anomaly_detection(std_data)

        print(f"now on {user} - ", end="")
        model.visualize(data, symptom_date, diagnosis_date, user)
        print("Passed")
    except Exception as e:
        print(f"{user} raised Execption as {e}")
        err_count += 1
        pass

print(f"Done, error raised {err_count} times.")