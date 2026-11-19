import csv
def cv(path):
    out = {}
    for r in csv.DictReader(open(path)):
        if r["data_type"] == "random" and r["cutoff"] in ("1", "7", "10"):
            out[(int(r["n"]), int(r["cutoff"]))] = 100 * float(r["std_ms"]) / float(r["median_ms"])
    return out
a, b = cv("results/summary.csv"), cv("results_single/summary.csv")
print("N        CUTOFF  Gop ban sao  Khong gop")
for k in sorted(b):
    print(f"{k[0]:<8} {k[1]:<7} {a[k]:>9.1f}%  {b[k]:>8.1f}%")