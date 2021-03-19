import boto3
import botocore

class Storage:
    def __init__(self, bucket=None, aws_config=None):
        if not aws_config:
            aws_config = config.get('aws')
        if not bucket:
            self.bucket = aws_config.get('s3').get('buckets').get('default')
        else:
            self.bucket = bucket
        self.access_key_id = aws_config.get('access_key_id')
        self.secret_access_key = aws_config.get('secret_access_key')
        self.region_name = aws_config.get('region_name')
        self.connection = None


    def connect(self):
        self.connection = boto3.client(
            's3',
            aws_access_key_id=self.access_key_id,
            aws_secret_access_key=self.secret_access_key,
            region_name=self.region_name
        )


    def upload_file(self, file_location: str, object_name=None):
        if not self.connection:
            return False
        if not object_name:
            object_name = file_location

        try:
             self.connection.upload_file(file_location, self.bucket, object_name)
             return True
        except botocore.exceptions.ClientError as e:
            print(f's3 exception:\n\n{e}')


    def download_file(self, download_location: str, object_name: str):
        if self.connection:
            try:
                self.connection.download_file(self.bucket, object_name, download_location)
                return True
            except botocore.exceptions.ClientError as e:
                if e.response['Error']['Code'] == '404':
                    print(f'object does not exist at location: {download_location}')
                    return False
                else:
                    raise


    def delete_object(self, object_path: str):
        if self.connection:
            self.connection.delete_object(Bucket=self.bucket, Key=object_path)
            return True
