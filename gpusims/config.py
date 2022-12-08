import yaml


class Config:
    def __init__(self, name, path):
        assert path.is_dir()
        self.name = name
        self.path = path

    def __repr__(self):
        return "{}({})".format(self.__class__.__name__, self.path)


def parse_configs(path):
    configs = {}
    with open(str(path.absolute()), "r") as f:
        configs_yaml = yaml.load(f)
        # pprint(configs_yaml)
        for name, config in configs_yaml.items():
            configs[name.lower()] = Config(
                name=name,
                path=path.parent / config["path"],
            )
    return configs
