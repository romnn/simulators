#!/usr/bin/env python3

import re
import sys

if __name__ == "__main__":
    if len(sys.argv) < 3:
        print("usage: <source file> <output file>")
        sys.exit(1)
    source_file = sys.argv[1]
    output_file = sys.argv[2]

    source = ""
    with open(source_file, "r") as f:
        source = f.read()

    pattern = re.compile(r"^\s*(int|void|)?\s*main\s*\((.*)\)", re.MULTILINE)

    # some quick tests
    assert pattern.search("int main(int argc, char *argv[]) {}") is not None
    assert pattern.search("\n\nint main(int argc, char *argv[]) {}") is not None

    main = pattern.search(source)
    if main is None:
        print("no main function found")
        sys.exit(1)

    old_main = main.group(0)
    print("found: \t\t", old_main.strip())

    # want: GPUTejas_main(int, char**)
    ret = main.group(1)
    args = main.group(2)
    new_main = " ".join([ret, "GPUTejas_main", "(", args, ")"])
    print("replace: \t", new_main)

    start, end = main.span(0)
    assert end - start == len(old_main)

    before = source[:start]
    after = source[end:]
    new_source = before + new_main + after

    print("Preview:")
    print("=" * 10)
    print("...")
    pad = 100
    preview_start = max(0, start - pad)
    preview_end = min(start + len(new_main) + pad, len(new_source))
    print(new_source[preview_start:preview_end])
    print("...")
    print("=" * 10)

    print("Writing new source to ", output_file)
    with open(output_file, "w") as f:
        f.write(new_source)
