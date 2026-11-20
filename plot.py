import csv
import sys
from collections import defaultdict
from pathlib import Path

import matplotlib

matplotlib.use("Agg")
import matplotlib.pyplot as plt

out_dir = Path(sys.argv[1] if len(sys.argv) > 1 else "results")

rows = defaultdict(lambda: defaultdict(list))
with open(out_dir / "summary.csv", newline="") as f:
    for r in csv.DictReader(f):
        rows[r["data_type"]][int(r["n"])].append(
            (int(r["cutoff"]), float(r["median_ms"]), float(r["std_ms"]), float(r["ratio_vs_baseline"]))
        )

for data_type, by_n in rows.items():
    fig, ax = plt.subplots(figsize=(9, 5.5))
    for n in sorted(by_n):
        pts = sorted(by_n[n])
        ax.plot([p[0] for p in pts], [p[3] for p in pts], marker="o", markersize=4, label=f"N = {n:,}")
    ax.axhline(1.0, color="gray", linewidth=0.8)
    for c in (7, 10):
        ax.axvline(c, color="gray", linestyle="--", linewidth=0.8)
        ax.text(c, ax.get_ylim()[1], f" {c}", va="top", fontsize=9, color="gray")
    ax.set_xlabel("CUTOFF")
    ax.set_ylabel("Thoi gian median / thoi gian tai CUTOFF = 1")
    ax.set_title(f"Anh huong cua CUTOFF - du lieu: {data_type}")
    ax.grid(True, alpha=0.3)
    ax.legend()
    fig.tight_layout()
    fig.savefig(out_dir / f"cutoff_{data_type}.png", dpi=150)
    plt.close(fig)

if "random" in rows:
    by_n = rows["random"]
    ns = sorted(by_n)
    fig, ax = plt.subplots(figsize=(7.5, 5.5))
    for cutoff in (1, 7, 10):
        ys = [dict((p[0], p[1]) for p in by_n[n]).get(cutoff) for n in ns]
        if all(y is not None for y in ys):
            ax.plot(ns, ys, marker="o", label=f"CUTOFF = {cutoff}")
    ax.set_xscale("log")
    ax.set_yscale("log")
    ax.set_xlabel("N")
    ax.set_ylabel("Thoi gian median (ms)")
    ax.set_title("Thoi gian chay theo N - du lieu ngau nhien")
    ax.grid(True, which="both", alpha=0.3)
    ax.legend()
    fig.tight_layout()
    fig.savefig(out_dir / "runtime_vs_n.png", dpi=150)
    plt.close(fig)

print("Da luu do thi vao", out_dir.resolve())