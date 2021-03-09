import yaml

def file_config(path='/home/ritwik/git_hub/TreasureHunt/backend/config.yml'):
    with open(path, 'r') as f:
        return yaml.load(f, Loader=yaml.FullLoader)


class Config:
    def __init__(self, config_dict: dict):
        self.config_dict = config_dict


    def get(self, *args):
        # gets args from config dict
        val = self.config_dict.copy()
        for arg in args:
            try:
                val = val.get(arg)
            except AttributeError:
                return None
        if type(val) == dict:
            return Config(val)
        return val

config = Config(file_config())
