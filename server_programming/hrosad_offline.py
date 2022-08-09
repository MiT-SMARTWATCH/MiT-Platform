from math import gamma
import warnings
warnings.filterwarnings('ignore')
import sys 
import argparse
import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
import matplotlib.dates as mdates
#%matplotlib inline
import seaborn as sns
from statsmodels.tsa.seasonal import seasonal_decompose
from sklearn.preprocessing import StandardScaler
from sklearn.svm import OneClassSVM
from sklearn.metrics import accuracy_score, confusion_matrix, f1_score, recall_score


class HROSAD_offline:

    def HROS(self, df_hr, df_steps):

        # df_hr = df_hr.set_index('ts')
        df_hr.index.name = None
        df_hr.index = pd.to_datetime(df_hr.index)
        # df_steps = df_steps.set_index('ts')
        df_steps.index.name = None
        df_steps.index = pd.to_datetime(df_steps.index)

        df_steps['steps'] = df_steps['steps'].apply(lambda x: x + 1)

        # merge dataframes
        df1 = pd.merge(df_hr, df_steps, left_index=True, right_index=True)
        df1['heartrate'] = (df1['heartrate']/df1['steps']) 
        return df1

     # pre-processing ------------------------------------------------------

    def pre_processing(self, resting_heart_rate):

        # smooth data
        df_nonas = resting_heart_rate.dropna()
        df1_rom = df_nonas.rolling(350).mean() # 300혹은 350이 실험결과 제일 최적화됨
        # resample
        df2 = df1_rom.resample('1H').mean()
        df2 = df2.dropna()
        return df2

    # seasonality correction ------------------------------------------------------

    def seasonality_correction(self, heartrate, steps):

        # sdHR_decomposition = seasonal_decompose(heartrate, model='additive', freq=1)
        sdHR_decomposition = seasonal_decompose(heartrate, model='additive', period=1, extrapolate_trend='freq')
        sdSteps_decomposition = seasonal_decompose(steps, model='additive', period=1, extrapolate_trend='freq')
        sdHR_decomp = pd.DataFrame(sdHR_decomposition.resid + sdHR_decomposition.trend)
        sdHR_decomp.rename(columns={sdHR_decomp.columns[0]:'heartrate'}, inplace=True)
        sdSteps_decomp = pd.DataFrame(sdSteps_decomposition.resid + sdSteps_decomposition.trend)
        sdSteps_decomp.rename(columns={sdSteps_decomp.columns[0]:'steps_window_12'}, inplace=True)
        frames = [sdHR_decomp, sdSteps_decomp]
        data = pd.concat(frames, axis=1)
        return data

    # standardization ------------------------------------------------------

    def standardization(self, seasonality_corrected_data):

        data_scaled = StandardScaler().fit_transform(seasonality_corrected_data.values)
        data_scaled_features = pd.DataFrame(data_scaled, index=seasonality_corrected_data.index, columns=seasonality_corrected_data.columns)
        data_df = pd.DataFrame(data_scaled_features)
        data = pd.DataFrame(data_df).fillna(0)
        return data

    # train model and predict anomalies -----------------------------------

    def anomaly_detection(self, std_data):

        model =  OneClassSVM(nu=0.1, gamma=0.15)

        model.fit(std_data)
        preds = pd.DataFrame(model.predict(std_data))
        preds = preds.rename(lambda x: 'anomaly' if x == 0 else x, axis=1)
        data = std_data.reset_index()
        data = data.join(preds)
        print(data)
        return data

    # Visualization ------------------------------------------------------

    def visualize(self, results, device_id):

        try:
            with plt.style.context('fivethirtyeight'):
                fig, ax = plt.subplots(1, figsize=(80,15))
                a = results.loc[results['anomaly'] == -1, ('index', 'heartrate')] #anomaly
                b = a[(a['heartrate']> 0)]
                ax.bar(results['index'], results['heartrate'], linestyle='-',color='midnightblue' ,lw=6, width=0.01)
                ax.scatter(b['index'],b['heartrate'], color='red', label='Anomaly', s=500)
                # We change the fontsize of minor ticks label
                ax.tick_params(axis='both', which='major', color='blue', labelsize=60)
                ax.tick_params(axis='both', which='minor', color='blue', labelsize=60)
                ax.set_title('Result',fontweight="bold", size=50) # Title
                ax.set_ylabel('Std. HROS\n', fontsize = 50) # Y label
                ax.tick_params(axis='both', which='major', labelsize=60)
                ax.tick_params(axis='both', which='minor', labelsize=60)
                ax.xaxis.set_major_locator(mdates.DayLocator(interval=7))
                #ax.tick_params(labelrotation=90,fontsize=14)
                ax.grid(zorder=0)
                ax.grid(True)
                #plt.legend()
                plt.xticks(fontsize=30, rotation=90)
                plt.yticks(fontsize=50)
                ax.patch.set_facecolor('white')
                fig.patch.set_facecolor('white')
                plt.show()      
                file_path = device_id
                figure = fig.savefig(file_path, bbox_inches='tight')  
                # Anomaly results
                # b['Anomalies'] = myphd_id
                # b.to_csv(myphd_id_anomalies, mode='a', header=False)        
                # return figure

        except:
            with plt.style.context('fivethirtyeight'):
                fig, ax = plt.subplots(1, figsize=(80,15))
                a = results.loc[results['anomaly'] == -1, ('index', 'heartrate')] #anomaly
                b = a[(a['heartrate']> 0)]
                ax.bar(results['index'], results['heartrate'], linestyle='-',color='midnightblue' ,lw=6, width=0.01)
                ax.scatter(b['index'],b['heartrate'], color='red', label='Anomaly', s=1000)
                ax.tick_params(axis='both', which='major', color='blue', labelsize=60)
                ax.tick_params(axis='both', which='minor', color='blue', labelsize=60)
                ax.set_title('Result',fontweight="bold", size=50) # Title
                ax.set_ylabel('Std. HROS\n', fontsize = 50) # Y label
                ax.tick_params(axis='both', which='major', labelsize=60)
                ax.tick_params(axis='both', which='minor', labelsize=60)
                ax.xaxis.set_major_locator(mdates.DayLocator(interval=7))
                ax.grid(zorder=0)
                ax.grid(True)
                plt.xticks(fontsize=30, rotation=90)
                plt.yticks(fontsize=50)
                ax.patch.set_facecolor('white')
                fig.patch.set_facecolor('white')     
                # figure = fig.savefig(myphd_id_figure, bbox_inches='tight')  
                # # Anomaly results
                # b['Anomalies'] = myphd_id
                # b.to_csv(myphd_id_anomalies, mode='a', header=False)        
                # return figure
