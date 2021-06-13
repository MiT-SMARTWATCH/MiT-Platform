# 폴더 내에 저장된 파일들을 list 형식으로                
entries = os.listdir('/home/cho/Desktop/HROS-AD/COVID-19-Wearables')
# # entries = os.listdir('./COVID-19-Wearables')

users = []

for entry in entries:
    # user = ''
    # for i in range(len(entry)):
    #     if entry[i] != '_':
    #         user = user + entry[i]
    #     else:
    #         break
    user = entry[0:7]
    if not user in users:
        users.append(user)

# print(users)


# 사용자들의 anomaly data 분석 후 png file들 생성
for user in users:
    # print(user)
    model = HROSAD_offline()
    hr_data = f'/home/cho/Desktop/HROS-AD/COVID-19-Wearables/{user}_hr.csv'
    steps_data = f'/home/cho/Desktop/HROS-AD/COVID-19-Wearables/{user}_steps.csv'

    df1 = model.HROS(hr_data,steps_data)
    df2 = model.pre_processing(df1)

    sdHR = df2[['heartrate']]
    sdSteps = df2[['steps']]
    data_seasnCorec = model.seasonality_correction(sdHR, sdSteps)
    data_seasnCorec += 0.1
    std_data = model.standardization(data_seasnCorec)
    data = model.anomaly_detection(std_data)
    model.visualize(data, symptom_date, diagnosis_date, user)
    shutil.move(f'{user}.png', '/home/cho/Desktop/HROS-AD/COVID-19-Wearables/result')
