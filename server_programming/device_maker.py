import pandas as pd
from pandas._libs.tslibs import NullFrequencyError
import psycopg2
from sqlalchemy import create_engine
import logging
# Importing models and REST client class from Community Edition version
from tb_rest_client.rest_client_ce import *
from tb_rest_client.rest import ApiException

password = 7452

# PostgreSQL에 접속 가능한 engine 생성
def createEngineUser_info(password):
    engine_user_info = create_engine(f'postgresql://postgres:{password}@127.0.0.1:5432/user_info')
    return engine_user_info

def createEngineThingsboard(password):
    engine_Thingsboard = create_engine(f'postgresql://postgres:{password}@127.0.0.1:5432/thingsboard')
    return engine_Thingsboard

################# device make
def MakeDeviceForRegister(device, user_ID, password):
    engine_user_info = createEngineUser_info(password)

    # 예외처리 필요
    # user_ID = pd.read_sql_query(sql=f"SELECT user_id FROM user_device WHERE user_id = '{user_ID}'", con=engine_user_info)
    # user_ID = pd.read_sql_query(sql=f"SELECT 이름 FROM user_device WHERE 아이디 = '{user_ID}'", con=engine_user_info)

    device_name = user_ID + "_smartwatch"

    # Thingsboard API
    logging.basicConfig(level=logging.DEBUG,
                        format='%(asctime)s - %(levelname)s - %(module)s - %(lineno)d - %(message)s',
                        datefmt='%Y-%m-%d %H:%M:%S')

    # ThingsBoard URL
    url = "http://localhost:8080"

    # Default Tenant Administrator credentials
    username = "tenant@thingsboard.org"
    password = "tenant"

    # Creating the REST client object and make device as device_name
    with RestClientCE(base_url=url) as rest_client:
        try:
            # Auth with credentials
            rest_client.login(username=username, password=password)

            # creating a Device
            device = Device(name=device_name, type="thermometer")
            device = rest_client.save_device(device)

            logging.info(" Device was created:\n%r\n", device)
        except ApiException as e:
            logging.exception(e)

    engine_Thingsboard = createEngineThingsboard(password)

    # 만든 device의 device_id 및 token_key 찾기
    # device_id =  pd.read_sql_query(sql = f"SELECT id FROM device WHERE name = '{device_name}'",con=engine_Thingsboard)
    device_id =  pd.read_sql_query(sql = f"SELECT id FROM device WHERE name = '{device_name}'",con=engine_Thingsboard)
    device_id = device_id["id"][0]
    print(device_id)

    token_key = pd.read_sql_query(sql = f"SELECT credentials_id FROM device_credentials WHERE device_id = '{device_id}'",con=engine_Thingsboard)
    token_key = token_key["credentials_id"][0]
    print(token_key)

    # device의 id, token_key 를 회원가입 table에 업로드
    engine_user_info.execute(f"UPDATE user_device SET device_id = '{device_id}', token_key = '{token_key}' Where user_id = '{user_ID}';")

    engine_Thingsboard.dispose()
    engine_user_info.dispose()
