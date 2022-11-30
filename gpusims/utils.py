def dedup_stable(seq):
    seen = set()
    return [x for x in seq if not (x in seen or seen.add(x))]


def add_paths_to_env(env, paths):
    new_paths = os.environ.get(env, "").split(os.pathsep)
    new_paths += [str(p) for p in paths]
    # filter empty paths
    new_paths = [p for p in new_paths if len(p) > 0]
    new_paths = os.pathsep.join(dedup_stable(new_paths))
    os.environ[env] = new_paths
    print(f"{env}={new_paths}")
    return new_paths
