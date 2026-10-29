"""
plot_rq3.py - Tạo hình, bảng LaTeX và các con số cho phần pdqsort / RQ3 của paper (Cặp 3).

Đầu vào (CSV thô, 21 cột Bảng 4 của quy định chung):
    results/rq3.csv            <- run_rq3.ps1 (hoặc run_rq3.sh)
    results/pdq_ablation.csv   <- java -cp target/classes csd201.sort.PdqEvidence ablation
    results/pdq_jit.csv        <- java -cp target/classes csd201.sort.PdqEvidence jit
Đầu ra:
    results/figures/fig4_rq3_runtime.png       Hình 4: thời gian quick / pdq / noA / noB / lib tại N = 10^7
    results/figures/fig5_pdq_core_ablation.png Hình 5: số so sánh khi tắt từng cơ chế lõi (N = 10^5)
    paper/tables/rq3_runtime.tex               Bảng RQ3: median [min, max] và tỷ lệ t_quick / t_pdq
    paper/tables/rq3_counts.tex                Bảng số so sánh và max_depth tại N = 10^7
    paper/tables/rq3_killer.tex                Bảng mảng sát thủ (KILLER)
    paper/rq3_numbers.tex                      Macro \RQ... cho các con số trong lời văn

Cách chạy:  python plot_rq3.py [--rq3 results/rq3.csv] [--ablation results/pdq_ablation.csv]
Mọi con số trong paper phần RQ3 lấy từ các file trên; không sửa tay.
"""
import argparse
import math
import os

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import pandas as pd

ap = argparse.ArgumentParser()
ap.add_argument("--rq3", default=os.path.join("results", "rq3.csv"))
ap.add_argument("--ablation", default=os.path.join("results", "pdq_ablation.csv"))
ap.add_argument("--jit", default=os.path.join("results", "pdq_jit.csv"))
ap.add_argument("--fig-dir", default=os.path.join("results", "figures"))
ap.add_argument("--paper-dir", default="paper")
args = ap.parse_args()

os.makedirs(args.fig_dir, exist_ok=True)
os.makedirs(os.path.join(args.paper_dir, "tables"), exist_ok=True)

RQ3_CSV = args.rq3.replace("\\", "/")
ABL_CSV = args.ablation.replace("\\", "/")
SCRIPT = "plot\\_rq3.py"

CONFIGS = [  # (algorithm, variant, nhãn)
    ("quick", "base", "Quick Sort"),
    ("pdq", "base", "pdqsort"),
    ("pdq", "noA", "pdq noA"),
    ("pdq", "noB", "pdq noB"),
    ("lib-Arrays.sort", "base", "lib-Arrays.sort"),
]
DATASETS = [  # (dataset, params, nhãn ngắn, nhãn LaTeX)
    ("D1", "k=0", "D1", "$D_1$ ngẫu nhiên"),
    ("D2", "k=0", "D2", "$D_2$ tăng dần"),
    ("D3", "k=0", "D3", "$D_3$ gần tăng dần"),
    ("D4", "k=0", "D4", "$D_4$ giảm dần"),
    ("D5", "k=1000", "D5\nK=1000", "$D_5$, $K=1000$"),
    ("D5", "k=10", "D5\nK=10", "$D_5$, $K=10$"),
    ("D5", "k=2", "D5\nK=2", "$D_5$, $K=2$"),
]
COLORS = {  # một màu cố định cho mỗi cấu hình
    "Quick Sort": "#2a78d6", "pdqsort": "#eb6834", "pdq noA": "#1baf7a",
    "pdq noB": "#eda100", "lib-Arrays.sort": "#8c8b86",
}

# --------------------------------------------------------------------------
df = pd.read_csv(args.rq3)
df["params"] = df["params"].fillna("k=0")
timing = df[df["ops_count"].isna()].copy()       # lượt đo thời gian: ops_count để trống
counts = df[df["ops_count"].notna()].copy()      # lượt --count
machine = ", ".join(sorted(df["machine_id"].dropna().astype(str).unique()))
xmx = ", ".join(sorted(df["xmx"].dropna().astype(str).unique()))
commit = ", ".join(sorted(df["git_commit"].dropna().astype(str).unique()))


def tex(s):
    return str(s).replace("_", "\\_").replace("%", "\\%")


def fmt(v, d=1):
    """Số kiểu Việt Nam: dấu chấm phân nghìn, dấu phẩy thập phân."""
    if v is None or (isinstance(v, float) and math.isnan(v)):
        return "--"
    s = f"{v:,.{d}f}"
    return s.replace(",", "X").replace(".", ",").replace("X", ".")


def cell(algo, variant, ds, params, n):
    sub = timing[(timing.algorithm == algo) & (timing.variant == variant) &
                 (timing.dataset == ds) & (timing.params == params) & (timing.n == n)]
    if sub.empty:
        return None
    bad = sub[sub.status != "ok"]
    ok = sub[sub.status == "ok"]["elapsed_ms"].astype(float)
    return {"median": ok.median() if len(ok) else float("nan"),
            "min": ok.min() if len(ok) else float("nan"),
            "max": ok.max() if len(ok) else float("nan"),
            "runs": len(sub), "okruns": len(ok), "bad": sorted(bad.status.unique().tolist())}


def count_cell(algo, variant, ds, params, n):
    sub = counts[(counts.algorithm == algo) & (counts.variant == variant) &
                 (counts.dataset == ds) & (counts.params == params) & (counts.n == n) &
                 (counts.status == "ok")]
    if sub.empty:
        return None
    return {"ops": sub["ops_count"].astype(float).median(),
            "depth": sub["max_depth"].astype(float).median()}


N_BIG = int(df[df.dataset != "KILLER"]["n"].max())
N_TXT = "10^{%d}" % round(math.log10(N_BIG))

# ---------------------------------------------------------- Hình 4
fig, ax = plt.subplots(figsize=(7.2, 2.9), dpi=200)
width = 0.16
for j, (algo, var, lab) in enumerate(CONFIGS):
    xs, meds, lo, hi = [], [], [], []
    for i, (ds, pr, short, _) in enumerate(DATASETS):
        c = cell(algo, var, ds, pr, N_BIG)
        if c and not math.isnan(c["median"]):
            xs.append(i + (j - 2) * width)
            meds.append(c["median"])
            lo.append(c["median"] - c["min"])
            hi.append(c["max"] - c["median"])
    ax.bar(xs, meds, width * 0.92, color=COLORS[lab], label=lab,
           yerr=[lo, hi], error_kw={"elinewidth": 0.8, "capsize": 1.5, "ecolor": "#52514e"})
ax.set_yscale("log")
ax.set_xticks(range(len(DATASETS)))
ax.set_xticklabels([d[2] for d in DATASETS], fontsize=8.5)
ax.set_ylabel(f"Thời gian (ms, log), N = 1e{round(math.log10(N_BIG))}")
for s in ("top", "right"):
    ax.spines[s].set_visible(False)
ax.yaxis.grid(True, which="major", color="#e4e3df", linewidth=0.8)
ax.set_axisbelow(True)
ax.legend(frameon=False, fontsize=8, ncol=5, loc="upper center", bbox_to_anchor=(0.5, 1.14))
fig.tight_layout()
fig4 = os.path.join(args.fig_dir, "fig4_rq3_runtime.png")
fig.savefig(fig4)
plt.close(fig)

# ---------------------------------------------------------- Hình 5
ab = pd.read_csv(args.ablation)
ab_cfg = [("core", "Lõi đầy đủ"), ("no-ninther", "Tắt ninther"),
          ("no-break", "Tắt phá mẫu"), ("no-heap", "Tắt Heap Sort dự phòng")]
ab_data = [("D1", "D1"), ("D2", "D2"), ("D4", "D4"), ("D5-2", "D5 K=2"), ("organ", "Ống đàn\norgan")]
ab_col = ["#2a78d6", "#1baf7a", "#eda100", "#e34948"]
fig, ax = plt.subplots(figsize=(7.2, 2.7), dpi=200)
w = 0.2
for j, (cfg, lab) in enumerate(ab_cfg):
    vals = [float(ab[(ab.config == cfg) & (ab.data == d)]["comparisons"].iloc[0]) for d, _ in ab_data]
    ax.bar([i + (j - 1.5) * w for i in range(len(ab_data))], vals, w * 0.92, color=ab_col[j], label=lab)
ax.set_yscale("log")
ax.set_xticks(range(len(ab_data)))
ax.set_xticklabels([d[1] for d in ab_data], fontsize=8.5)
n_ab = int(ab["n"].iloc[0])
ax.set_ylabel(f"Số phép so sánh (log), N = 1e{round(math.log10(n_ab))}")
for s in ("top", "right"):
    ax.spines[s].set_visible(False)
ax.yaxis.grid(True, which="major", color="#e4e3df", linewidth=0.8)
ax.set_axisbelow(True)
ax.legend(frameon=False, fontsize=8, ncol=4, loc="upper center", bbox_to_anchor=(0.5, 1.13))
fig.tight_layout()
fig5 = os.path.join(args.fig_dir, "fig5_pdq_core_ablation.png")
fig.savefig(fig5)
plt.close(fig)

# ---------------------------------------------------------- Bảng thời gian RQ3
rows = []
incomplete = []
for ds, pr, _, lab in DATASETS:
    cells = []
    for algo, var, _ in CONFIGS:
        c = cell(algo, var, ds, pr, N_BIG)
        if c is None:
            cells.append("--")
        elif math.isnan(c["median"]):
            cells.append("/".join(c["bad"]))
        else:
            star = "$^{*}$" if c["bad"] else ""
            cells.append(f"{fmt(c['median'])}{star} \\scriptsize[{fmt(c['min'])}; {fmt(c['max'])}]")
            if c["bad"]:
                incomplete.append(f"{c['runs'] - c['okruns']} lượt {'/'.join(c['bad'])}")
    q = cell("quick", "base", ds, pr, N_BIG)
    p = cell("pdq", "base", ds, pr, N_BIG)
    ratio = (q["median"] / p["median"]) if (q and p) else float("nan")
    rows.append(f"{lab} & " + " & ".join(cells) + f" & {fmt(ratio, 2)} \\\\")

runs_per_cell = int(timing[timing.n == N_BIG].groupby(["algorithm", "variant", "dataset", "params"]).size().max())
with open(os.path.join(args.paper_dir, "tables", "rq3_runtime.tex"), "w", encoding="utf-8") as f:
    f.write("% Sinh tự động bởi plot_rq3.py từ " + RQ3_CSV + ". Không sửa tay.\n")
    f.write("\\begin{table*}[t]\n\\caption{Thời gian sắp xếp (ms) tại $N = " + N_TXT + "$, median "
            "[min; max], 3 seed $\\times$ 3 lượt ($D_2, D_4$: 3 lượt). quick/pdq = median$_{\\text{quick}}$/median$_{\\text{pdq}}$, "
            "mốc Quick Sort, cùng $N$, dữ liệu, máy " + tex(machine) + ", \\texttt{-Xmx" + tex(xmx) +
            "}. Nguồn: \\texttt{" + tex(RQ3_CSV) + "} (\\texttt{run\\_rq3.ps1}); bảng: \\texttt{" +
            SCRIPT + "}.}\n\\label{tab:rq3}\n\\centering\n"
            "\\resizebox{\\textwidth}{!}{\\begin{tabular}{@{}lrrrrrr@{}}\n\\toprule\n"
            "\\textbf{Dữ liệu} & \\textbf{Quick Sort} & \\textbf{pdqsort} & \\textbf{noA} & \\textbf{noB} & "
            "\\textbf{lib-Arrays.sort} & \\textbf{quick/pdq} \\\\\n\\midrule\n")
    f.write("\n".join(rows) + "\n\\bottomrule\n\\end{tabular}}\n")
    if incomplete:
        f.write("\\par\\vspace{2pt}{\\footnotesize $^{*}$ Có lượt không hoàn thành (" + "; ".join(incomplete) +
                ", ghi trong CSV); median, min, max và tỷ lệ chỉ tính trên các lượt \\texttt{ok} (Mục 6.7).}\n")
    f.write("\\end{table*}\n")

# ---------------------------------------------------------- Bảng số so sánh
crow = []
for ds, pr, _, lab in DATASETS:
    cs = []
    for algo, var, _ in CONFIGS[:4]:
        c = count_cell(algo, var, ds, pr, N_BIG)
        cs.append("--" if c is None else f"{fmt(c['ops'] / (N_BIG * math.log2(N_BIG)), 2)} ({int(c['depth'])})")
    crow.append(f"{lab} & " + " & ".join(cs) + " \\\\")
with open(os.path.join(args.paper_dir, "tables", "rq3_counts.tex"), "w", encoding="utf-8") as f:
    f.write("% Sinh tự động bởi plot_rq3.py từ " + RQ3_CSV + ". Không sửa tay.\n")
    f.write("\\begin{table}[htbp]\n\\caption{Số phép so sánh chia cho $N\\log_2 N$ và (max\\_depth) ở lượt "
            "\\texttt{--count}, $N = " + N_TXT + "$, median theo seed. Nguồn: \\texttt{" + tex(RQ3_CSV) +
            "} (dòng có ops\\_count), tạo bằng \\texttt{run\\_rq3.ps1}, bảng bằng \\texttt{" + SCRIPT +
            "}.}\n\\label{tab:rq3counts}\n\\centering\n\\resizebox{\\columnwidth}{!}{\\begin{tabular}{lrrrr}\n\\toprule\n"
            "\\textbf{Dữ liệu} & \\textbf{quick} & \\textbf{pdq} & \\textbf{noA} & \\textbf{noB} \\\\\n\\midrule\n")
    f.write("\n".join(crow) + "\n\\bottomrule\n\\end{tabular}}\n\\end{table}\n")

# ---------------------------------------------------------- Bảng KILLER
k = df[df.dataset == "KILLER"]
krow = []
for n in sorted(k["n"].unique()):
    for algo, lab in (("quick", "Quick Sort"), ("pdq", "pdqsort")):
        t = k[(k.algorithm == algo) & (k.n == n) & k.ops_count.isna()]
        c = k[(k.algorithm == algo) & (k.n == n) & k.ops_count.notna()]
        st = t["status"].iloc[0] if len(t) else "--"
        ms = fmt(float(t["elapsed_ms"].iloc[0]), 2) if len(t) and st == "ok" else "--"
        ops = fmt(float(c["ops_count"].iloc[0]), 0) if len(c) and c["status"].iloc[0] == "ok" else "--"
        dep = fmt(float(c["max_depth"].iloc[0]), 0) if len(c) and c["status"].iloc[0] == "ok" else "--"
        krow.append(f"{lab} & {fmt(n, 0)} & {ms} & {ops} & {dep} & {tex(st)} \\\\")
with open(os.path.join(args.paper_dir, "tables", "rq3_killer.tex"), "w", encoding="utf-8") as f:
    f.write("% Sinh tự động bởi plot_rq3.py từ " + RQ3_CSV + ". Không sửa tay.\n")
    f.write("\\begin{table}[htbp]\n\\caption{Mảng sát thủ KILLER (McIlroy \\cite{mcilroy1999killer}), repeat = 1; "
            "$N = 10^6$ không chạy vì bộ sinh tốn $O(N^2)$. Nguồn: \\texttt{" +
            tex(RQ3_CSV) + "} (dataset = KILLER), tạo bằng \\texttt{run\\_rq3.ps1}, bảng bằng \\texttt{" +
            SCRIPT + "}.}\n\\label{tab:killer}\n\\centering\n\\resizebox{\\columnwidth}{!}{\\begin{tabular}{lrrrrl}\n\\toprule\n"
            "\\textbf{Thuật toán} & $N$ & \\textbf{ms} & \\textbf{So sánh} & \\textbf{max\\_depth} & "
            "\\textbf{status} \\\\\n\\midrule\n")
    f.write("\n".join(krow) + "\n\\bottomrule\n\\end{tabular}}\n\\end{table}\n")


# ---------------------------------------------------------- Macro cho lời văn
def med(algo, var, ds, pr):
    c = cell(algo, var, ds, pr, N_BIG)
    return c["median"] if c else float("nan")


N_SMALL = int(df[df.dataset != "KILLER"]["n"].min())


def med_small(algo, var, ds, pr):
    c = cell(algo, var, ds, pr, N_SMALL)
    return c["median"] if c else float("nan")


def ops(algo, var, ds, pr):
    c = count_cell(algo, var, ds, pr, N_BIG)
    return c["ops"] if c else float("nan")


m = {}
for ds, pr, tag in (("D1", "k=0", "Done"), ("D2", "k=0", "Dtwo"), ("D3", "k=0", "Dthree"),
                    ("D4", "k=0", "Dfour"), ("D5", "k=10", "Kten"), ("D5", "k=2", "Ktwo")):
    q, p, na, nb = med("quick", "base", ds, pr), med("pdq", "base", ds, pr), med("pdq", "noA", ds, pr), med("pdq", "noB", ds, pr)
    m[f"RQquickOverPdq{tag}"] = fmt(q / p, 2)
    m[f"RQnoAOverPdq{tag}"] = fmt(na / p, 2)
    m[f"RQnoBOverPdq{tag}"] = fmt(nb / p, 2)
    m[f"RQpdqMs{tag}"] = fmt(p, 1)
    m[f"RQquickMs{tag}"] = fmt(q, 1)
    m[f"RQnoAMs{tag}"] = fmt(na, 1)
    m[f"RQnoBMs{tag}"] = fmt(nb, 1)
    m[f"RQlibMs{tag}"] = fmt(med("lib-Arrays.sort", "base", ds, pr), 1)
    m[f"RQpdqOpsPerN{tag}"] = fmt(ops("pdq", "base", ds, pr) / N_BIG, 2)
    m[f"RQnoAOpsPerN{tag}"] = fmt(ops("pdq", "noA", ds, pr) / N_BIG, 2)
    m[f"RQnoBOpsPerN{tag}"] = fmt(ops("pdq", "noB", ds, pr) / N_BIG, 2)
# KILLER
for n_k, tagn in ((10000, "Four"), (100000, "Five")):
    for algo, taga in (("quick", "Quick"), ("pdq", "Pdq")):
        c = k[(k.algorithm == algo) & (k.n == n_k) & k.ops_count.notna()]
        t = k[(k.algorithm == algo) & (k.n == n_k) & k.ops_count.isna()]
        okc = len(c) and c["status"].iloc[0] == "ok"
        m[f"RQkiller{taga}Ops{tagn}"] = fmt(float(c["ops_count"].iloc[0]), 0) if okc else "--"
        m[f"RQkiller{taga}Depth{tagn}"] = fmt(float(c["max_depth"].iloc[0]), 0) if okc else "--"
        m[f"RQkiller{taga}Status{tagn}"] = tex(t["status"].iloc[0]) if len(t) else "--"
# JIT (cùng JVM, lần gọi 5..8)
JIT_CSV = args.jit.replace("\\", "/")
if os.path.exists(args.jit):
    jt = pd.read_csv(args.jit)
    for v, tv in (("base", "Base"), ("noA", "NoA"), ("noB", "NoB")):
        for d, td in (("D2", "Dtwo"), ("D3", "Dthree"), ("D5-2", "Ktwo")):
            sub = jt[(jt.variant == v) & (jt.dataset == d) & (jt.call >= 5)]["elapsed_ms"]
            m[f"RQjit{tv}{td}"] = fmt(sub.median(), 1) if len(sub) else "--"
    m["RQjitCsv"] = tex(JIT_CSV)
m["RQmachine"] = tex(machine)
m["RQxmx"] = tex(xmx)
m["RQcommit"] = tex(commit)
m["RQbigN"] = "$" + N_TXT + "$"
m["RQsmallN"] = "$10^{%d}$" % round(math.log10(N_SMALL))
m["RQnoAOverPdqDtwoSmall"] = fmt(med_small("pdq", "noA", "D2", "k=0") / med_small("pdq", "base", "D2", "k=0"), 2)
_cs_a = count_cell("pdq", "noA", "D2", "k=0", N_SMALL)
_cs_b = count_cell("pdq", "base", "D2", "k=0", N_SMALL)
m["RQnoAOpsOverPdqDtwoSmall"] = fmt(_cs_a["ops"] / _cs_b["ops"], 1) if (_cs_a and _cs_b) else "--"
m["RQnoAOverPdqDfourSmall"] = fmt(med_small("pdq", "noA", "D4", "k=0") / med_small("pdq", "base", "D4", "k=0"), 2)
m["RQrunsPerCell"] = str(runs_per_cell)
with open(os.path.join(args.paper_dir, "rq3_numbers.tex"), "w", encoding="utf-8") as f:
    f.write("% Sinh tự động bởi plot_rq3.py từ " + RQ3_CSV + ". Không sửa tay.\n")
    for key, val in m.items():
        f.write("\\newcommand{\\" + key + "}{" + val + "}\n")

print("Generated:", fig4, fig5)
print("Generated:", os.path.join(args.paper_dir, "tables", "rq3_runtime.tex"),
      os.path.join(args.paper_dir, "tables", "rq3_counts.tex"),
      os.path.join(args.paper_dir, "tables", "rq3_killer.tex"),
      os.path.join(args.paper_dir, "rq3_numbers.tex"))

# ---------------------------------------------------------- Tự kiểm tra lời văn trong paper/main.tex
# Lời văn mục RQ3 mô tả CHIỀU của kết quả. Nếu số trên máy đo chính đi ngược chiều, câu đó phải sửa.
warn = []
r_noA_d2 = med("pdq", "noA", "D2", "k=0") / med("pdq", "base", "D2", "k=0")
if not r_noA_d2 > 1.5:
    warn.append(f"t_noA/t_pdq tren D2 tai N lon = {r_noA_d2:.2f} <= 1.5: sua doan 'Co che A'.")
r_small = med_small("pdq", "noA", "D2", "k=0") / med_small("pdq", "base", "D2", "k=0")
if not r_small < 3:
    warn.append(f"t_noA/t_pdq tren D2 tai N nho = {r_small:.2f} >= 3: JIT khong che loi ich -> sua/bo doan 'JIT che loi ich' va cau 'trai du doan'.")
r_noB_k2 = med("pdq", "noB", "D5", "k=2") / med("pdq", "base", "D5", "k=2")
if not r_noB_k2 > 1:
    warn.append(f"t_noB/t_pdq tren D5 K=2 = {r_noB_k2:.2f} <= 1: sua doan 'Co che B'.")
if ops("pdq", "base", "D2", "k=0") > 3 * N_BIG:
    warn.append("pdq base tren D2 ton hon 3N so sanh: kiem tra lai co che A.")
if m.get("RQkillerQuickStatusFive") != "crash":
    warn.append("Quick Sort KILLER N=1e5 khong phai crash: sua cau 'StackOverflowError' (muc mang sat thu).")
if os.path.exists(args.jit):
    jb = jt[(jt.variant == "base") & (jt.dataset == "D2") & (jt.call >= 5)]["elapsed_ms"].median()
    ja = jt[(jt.variant == "noA") & (jt.dataset == "D2") & (jt.call >= 5)]["elapsed_ms"].median()
    if not ja > 2 * jb:
        warn.append(f"JIT: noA ({ja:.1f} ms) khong cham hon 2 lan base ({jb:.1f} ms): sua doan 'JIT che loi ich'.")
else:
    warn.append("Chua co results/pdq_jit.csv: chay 'java -cp target/classes csd201.sort.PdqEvidence jit'.")
bad_status = df[(df.status != "ok") & (df.dataset != "KILLER")]
if len(bad_status):
    warn.append(f"{len(bad_status)} dong RQ3 (ngoai KILLER) co status khac ok: xem lai truoc khi dung so lieu.")
print("\n=== KIEM TRA LOI VAN ===")
print("\n".join("CANH BAO: " + w for w in warn) if warn else "Khop: loi van muc RQ3 dung voi so lieu.")
