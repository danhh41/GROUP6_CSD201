"""
generate_compare_img.py
========================
Tạo bộ biểu đồ so sánh RQ1: Quick Sort, Merge Sort, Heap Sort, Radix Sort
theo thời gian thực thi, bộ nhớ phụ và số thao tác khi N tăng.

Nguồn dữ liệu: results/summary_rq1.csv
Thư mục xuất : results/compare_img/
"""

import os
import sys
import pandas as pd
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import matplotlib.ticker as ticker
import numpy as np

# ──────────────────────────────────────────────
# Cấu hình đường dẫn
# ──────────────────────────────────────────────
CSV_PATH = os.path.join("results", "summary_rq1.csv")
OUT_DIR  = os.path.join("results", "compare_img")
os.makedirs(OUT_DIR, exist_ok=True)

# ──────────────────────────────────────────────
# Đọc dữ liệu
# ──────────────────────────────────────────────
df = pd.read_csv(CSV_PATH)
df["n"] = pd.to_numeric(df["n"], errors="coerce")
df["median_ms"]   = pd.to_numeric(df["median_ms"],   errors="coerce")
df["min_ms"]      = pd.to_numeric(df["min_ms"],      errors="coerce")
df["max_ms"]      = pd.to_numeric(df["max_ms"],      errors="coerce")
df["comparisons"] = pd.to_numeric(df["comparisons"], errors="coerce")
df["operations"]  = pd.to_numeric(df["operations"],  errors="coerce")
df["aux_memory_mb"] = pd.to_numeric(df["aux_memory_mb"], errors="coerce")

# Chỉ lấy 4 thuật toán so sánh chính (bỏ lib-Arrays.sort)
ALGOS = ["quick", "merge", "heap", "radix"]
ALGO_LABELS = {
    "quick": "Quick Sort",
    "merge": "Merge Sort",
    "heap":  "Heap Sort",
    "radix": "Radix Sort",
}
COLORS = {
    "quick": "#2196F3",   # blue
    "merge": "#4CAF50",   # green
    "heap":  "#FF9800",   # orange
    "radix": "#E91E63",   # pink-red
}
MARKERS = {
    "quick": "o",
    "merge": "s",
    "heap":  "^",
    "radix": "D",
}

df4 = df[df["algorithm"].isin(ALGOS)].copy()

N_LABELS = {
    100_000:    "10⁵",
    1_000_000:  "10⁶",
    5_000_000:  "5×10⁶",
    10_000_000: "10⁷",
    20_000_000: "2×10⁷",
    50_000_000: "5×10⁷",
}

# ──────────────────────────────────────────────
# Helper: định dạng trục Y tự động (K / M / B)
# ──────────────────────────────────────────────
def human_fmt(x, _):
    if x >= 1e9: return f"{x/1e9:.1f}B"
    if x >= 1e6: return f"{x/1e6:.1f}M"
    if x >= 1e3: return f"{x/1e3:.0f}K"
    return str(int(x))

def n_label(n):
    return N_LABELS.get(int(n), f"{int(n):,}")

def x_labels(ns):
    return [n_label(n) for n in ns]

SORTED_NS = sorted(df4["n"].dropna().unique())

# ══════════════════════════════════════════════════════════════════════════════
# BIỂU ĐỒ 1 – Thời gian thực thi trung vị (ms) – Trục tuyến tính
# ══════════════════════════════════════════════════════════════════════════════
fig, ax = plt.subplots(figsize=(10, 5.5))
for algo in ALGOS:
    sub = df4[df4["algorithm"] == algo].sort_values("n")
    if sub.empty: continue
    ns_  = sub["n"].values
    meds = sub["median_ms"].values
    lo_  = meds - sub["min_ms"].values
    hi_  = sub["max_ms"].values - meds
    ax.errorbar(
        range(len(ns_)), meds,
        yerr=[lo_, hi_],
        label=ALGO_LABELS[algo],
        color=COLORS[algo],
        marker=MARKERS[algo],
        linewidth=2, markersize=7,
        capsize=4, capthick=1.5,
    )

ax.set_xticks(range(len(SORTED_NS)))
ax.set_xticklabels(x_labels(SORTED_NS), fontsize=11)
ax.set_xlabel("Quy mô mảng N", fontsize=12)
ax.set_ylabel("Thời gian thực thi (ms)", fontsize=12)
ax.set_title("Hình 1 – So sánh thời gian thực thi (trục tuyến tính) – Dữ liệu ngẫu nhiên D1", fontsize=13, pad=12)
ax.legend(fontsize=11)
ax.yaxis.set_major_formatter(ticker.FuncFormatter(human_fmt))
ax.grid(axis="y", linestyle="--", alpha=0.5)
fig.tight_layout()
fig.savefig(os.path.join(OUT_DIR, "fig1_time_linear.png"), dpi=150)
plt.close(fig)
print("✓ fig1_time_linear.png")


# ══════════════════════════════════════════════════════════════════════════════
# BIỂU ĐỒ 2 – Thời gian thực thi trung vị (ms) – Thang Log-Log
# ══════════════════════════════════════════════════════════════════════════════
fig, ax = plt.subplots(figsize=(10, 5.5))
for algo in ALGOS:
    sub = df4[df4["algorithm"] == algo].sort_values("n")
    if sub.empty: continue
    ax.plot(
        sub["n"], sub["median_ms"],
        label=ALGO_LABELS[algo],
        color=COLORS[algo],
        marker=MARKERS[algo],
        linewidth=2, markersize=7,
    )
    # Vùng min-max mờ
    ax.fill_between(sub["n"], sub["min_ms"], sub["max_ms"],
                    color=COLORS[algo], alpha=0.1)

ax.set_xscale("log")
ax.set_yscale("log")
ax.set_xlabel("Quy mô mảng N (thang log)", fontsize=12)
ax.set_ylabel("Thời gian (ms, thang log)", fontsize=12)
ax.set_title("Hình 2 – Quy luật tăng trưởng thời gian (Log-Log) – Dữ liệu ngẫu nhiên D1", fontsize=13, pad=12)
ax.legend(fontsize=11)
ax.grid(True, which="both", linestyle="--", alpha=0.4)
# Nhãn trục X hiển thị tên thân thiện
ax.set_xticks(SORTED_NS)
ax.set_xticklabels(x_labels(SORTED_NS), fontsize=10)
ax.xaxis.set_minor_formatter(ticker.NullFormatter())
fig.tight_layout()
fig.savefig(os.path.join(OUT_DIR, "fig2_time_loglog.png"), dpi=150)
plt.close(fig)
print("✓ fig2_time_loglog.png")


# ══════════════════════════════════════════════════════════════════════════════
# BIỂU ĐỒ 3 – Thời gian (ms) theo từng N – Grouped Bar Chart
# ══════════════════════════════════════════════════════════════════════════════
n_vals = SORTED_NS
x = np.arange(len(n_vals))
width = 0.19

fig, ax = plt.subplots(figsize=(12, 5.5))
for i, algo in enumerate(ALGOS):
    vals = []
    for nv in n_vals:
        row = df4[(df4["algorithm"] == algo) & (df4["n"] == nv)]
        vals.append(row["median_ms"].values[0] if not row.empty else 0)
    bars = ax.bar(x + i * width - 1.5*width, vals, width,
                  label=ALGO_LABELS[algo], color=COLORS[algo], alpha=0.88)

ax.set_xticks(x)
ax.set_xticklabels(x_labels(n_vals), fontsize=11)
ax.set_xlabel("Quy mô mảng N", fontsize=12)
ax.set_ylabel("Thời gian thực thi (ms)", fontsize=12)
ax.set_title("Hình 3 – Thời gian thực thi theo từng N (Grouped Bar Chart)", fontsize=13, pad=12)
ax.legend(fontsize=11)
ax.yaxis.set_major_formatter(ticker.FuncFormatter(human_fmt))
ax.grid(axis="y", linestyle="--", alpha=0.4)
fig.tight_layout()
fig.savefig(os.path.join(OUT_DIR, "fig3_time_bar.png"), dpi=150)
plt.close(fig)
print("✓ fig3_time_bar.png")


# ══════════════════════════════════════════════════════════════════════════════
# BIỂU ĐỒ 4 – Bộ nhớ phụ (MB) – Line Chart
# ══════════════════════════════════════════════════════════════════════════════
fig, ax = plt.subplots(figsize=(10, 5.5))
for algo in ALGOS:
    sub = df4[df4["algorithm"] == algo].sort_values("n")
    if sub.empty: continue
    ax.plot(
        range(len(sub["n"])), sub["aux_memory_mb"],
        label=f"{ALGO_LABELS[algo]} [{sub['aux_complexity'].values[0]}]",
        color=COLORS[algo],
        marker=MARKERS[algo],
        linewidth=2, markersize=7,
    )

ax.set_xticks(range(len(SORTED_NS)))
ax.set_xticklabels(x_labels(SORTED_NS), fontsize=11)
ax.set_xlabel("Quy mô mảng N", fontsize=12)
ax.set_ylabel("Bộ nhớ phụ sử dụng (MB)", fontsize=12)
ax.set_title("Hình 4 – Bộ nhớ phụ theo N (MB) – Dữ liệu ngẫu nhiên D1", fontsize=13, pad=12)
ax.legend(fontsize=11)
ax.grid(axis="y", linestyle="--", alpha=0.5)
fig.tight_layout()
fig.savefig(os.path.join(OUT_DIR, "fig4_memory_mb.png"), dpi=150)
plt.close(fig)
print("✓ fig4_memory_mb.png")


# ══════════════════════════════════════════════════════════════════════════════
# BIỂU ĐỒ 5 – Bộ nhớ phụ (MB) theo N – Grouped Bar Chart
# ══════════════════════════════════════════════════════════════════════════════
fig, ax = plt.subplots(figsize=(12, 5.5))
for i, algo in enumerate(ALGOS):
    vals = []
    for nv in n_vals:
        row = df4[(df4["algorithm"] == algo) & (df4["n"] == nv)]
        vals.append(row["aux_memory_mb"].values[0] if not row.empty else 0)
    ax.bar(x + i * width - 1.5*width, vals, width,
           label=ALGO_LABELS[algo], color=COLORS[algo], alpha=0.88)

ax.set_xticks(x)
ax.set_xticklabels(x_labels(n_vals), fontsize=11)
ax.set_xlabel("Quy mô mảng N", fontsize=12)
ax.set_ylabel("Bộ nhớ phụ (MB)", fontsize=12)
ax.set_title("Hình 5 – Bộ nhớ phụ theo N (Grouped Bar Chart)", fontsize=13, pad=12)
ax.legend(fontsize=11)
ax.grid(axis="y", linestyle="--", alpha=0.4)
fig.tight_layout()
fig.savefig(os.path.join(OUT_DIR, "fig5_memory_bar.png"), dpi=150)
plt.close(fig)
print("✓ fig5_memory_bar.png")


# ══════════════════════════════════════════════════════════════════════════════
# BIỂU ĐỒ 6 – Số phép so sánh (quick, merge, heap) – Line Chart, thang log
# ══════════════════════════════════════════════════════════════════════════════
CMP_ALGOS = ["quick", "merge", "heap"]
fig, ax = plt.subplots(figsize=(10, 5.5))
for algo in CMP_ALGOS:
    sub = df4[df4["algorithm"] == algo].sort_values("n")
    sub = sub[sub["comparisons"].notna() & (sub["comparisons"] > 0)]
    if sub.empty: continue
    ax.plot(
        sub["n"], sub["comparisons"],
        label=ALGO_LABELS[algo],
        color=COLORS[algo],
        marker=MARKERS[algo],
        linewidth=2, markersize=7,
    )

# Đường lý thuyết tham chiếu N log2 N
ns_ref = np.array(SORTED_NS, dtype=float)
ref_nlogn = ns_ref * np.log2(ns_ref)
ax.plot(ns_ref, ref_nlogn, "k--", linewidth=1.2, alpha=0.5, label="Lý thuyết N·log₂N")

ax.set_xscale("log")
ax.set_yscale("log")
ax.set_xlabel("Quy mô mảng N (thang log)", fontsize=12)
ax.set_ylabel("Số phép so sánh khóa (thang log)", fontsize=12)
ax.set_title("Hình 6 – Số phép so sánh khóa (Quick/Merge/Heap) theo N", fontsize=13, pad=12)
ax.legend(fontsize=11)
ax.grid(True, which="both", linestyle="--", alpha=0.4)
ax.set_xticks(SORTED_NS)
ax.set_xticklabels(x_labels(SORTED_NS), fontsize=10)
ax.xaxis.set_minor_formatter(ticker.NullFormatter())
ax.yaxis.set_major_formatter(ticker.FuncFormatter(human_fmt))
fig.tight_layout()
fig.savefig(os.path.join(OUT_DIR, "fig6_comparisons_loglog.png"), dpi=150)
plt.close(fig)
print("✓ fig6_comparisons_loglog.png")


# ══════════════════════════════════════════════════════════════════════════════
# BIỂU ĐỒ 7 – Số thao tác Radix Sort (12N đọc/ghi) – Line Chart, thang log
# ══════════════════════════════════════════════════════════════════════════════
fig, ax = plt.subplots(figsize=(10, 5.5))
sub_r = df4[df4["algorithm"] == "radix"].sort_values("n")
sub_r = sub_r[sub_r["operations"].notna() & (sub_r["operations"] > 0)]
ax.plot(
    sub_r["n"], sub_r["operations"],
    label="Radix Sort (12N thao tác đọc/ghi)",
    color=COLORS["radix"],
    marker=MARKERS["radix"],
    linewidth=2, markersize=7,
)

# Đường lý thuyết 12N
ns_ref = np.array(SORTED_NS, dtype=float)
ax.plot(ns_ref, 12 * ns_ref, "r--", linewidth=1.2, alpha=0.6, label="Lý thuyết 12·N")

# Đặt thêm quick/merge/heap để so sánh (mờ)
for algo in ["quick", "merge", "heap"]:
    sub = df4[df4["algorithm"] == algo].sort_values("n")
    sub = sub[sub["comparisons"].notna() & (sub["comparisons"] > 0)]
    ax.plot(sub["n"], sub["comparisons"],
            label=f"{ALGO_LABELS[algo]} (so sánh khóa)",
            color=COLORS[algo], marker=MARKERS[algo],
            linewidth=1.5, markersize=6, alpha=0.5, linestyle="--")

ax.set_xscale("log")
ax.set_yscale("log")
ax.set_xlabel("Quy mô mảng N (thang log)", fontsize=12)
ax.set_ylabel("Số thao tác (thang log)", fontsize=12)
ax.set_title("Hình 7 – Thao tác Radix (12N đọc/ghi) so với phép so sánh khóa (Log-Log)", fontsize=12, pad=12)
ax.legend(fontsize=10)
ax.grid(True, which="both", linestyle="--", alpha=0.4)
ax.set_xticks(SORTED_NS)
ax.set_xticklabels(x_labels(SORTED_NS), fontsize=10)
ax.xaxis.set_minor_formatter(ticker.NullFormatter())
ax.yaxis.set_major_formatter(ticker.FuncFormatter(human_fmt))
fig.tight_layout()
fig.savefig(os.path.join(OUT_DIR, "fig7_radix_ops_vs_comparisons.png"), dpi=150)
plt.close(fig)
print("✓ fig7_radix_ops_vs_comparisons.png")


# ══════════════════════════════════════════════════════════════════════════════
# BIỂU ĐỒ 8 – Heatmap: Thời gian thực thi (ms) – Algorithm × N
# ══════════════════════════════════════════════════════════════════════════════
import matplotlib.colors as mcolors

pivot = df4.pivot_table(index="algorithm", columns="n", values="median_ms")
# Sắp xếp hàng theo thứ tự tự nhiên
row_order = [a for a in ALGOS if a in pivot.index]
pivot = pivot.loc[row_order]

fig, ax = plt.subplots(figsize=(11, 4))
im = ax.imshow(pivot.values, aspect="auto", cmap="YlOrRd")
ax.set_xticks(range(len(pivot.columns)))
ax.set_xticklabels(x_labels(pivot.columns), fontsize=11)
ax.set_yticks(range(len(pivot.index)))
ax.set_yticklabels([ALGO_LABELS[a] for a in pivot.index], fontsize=11)
ax.set_xlabel("Quy mô mảng N", fontsize=12)
ax.set_title("Hình 8 – Heatmap: Thời gian thực thi (ms) – Algorithm × N", fontsize=13, pad=12)
plt.colorbar(im, ax=ax, label="Thời gian (ms)")
# Ghi số vào từng ô
for r in range(pivot.shape[0]):
    for c in range(pivot.shape[1]):
        val = pivot.values[r, c]
        if not np.isnan(val):
            txt = f"{val:,.0f}" if val >= 10 else f"{val:.2f}"
            ax.text(c, r, txt, ha="center", va="center", fontsize=8,
                    color="black" if val < pivot.values.max() * 0.6 else "white")
fig.tight_layout()
fig.savefig(os.path.join(OUT_DIR, "fig8_heatmap_time.png"), dpi=150)
plt.close(fig)
print("✓ fig8_heatmap_time.png")


# ══════════════════════════════════════════════════════════════════════════════
# BIỂU ĐỒ 9 – Heatmap: Bộ nhớ phụ (MB) – Algorithm × N
# ══════════════════════════════════════════════════════════════════════════════
pivot_mem = df4.pivot_table(index="algorithm", columns="n", values="aux_memory_mb")
pivot_mem = pivot_mem.loc[[a for a in ALGOS if a in pivot_mem.index]]

fig, ax = plt.subplots(figsize=(11, 4))
im = ax.imshow(pivot_mem.values, aspect="auto", cmap="Blues")
ax.set_xticks(range(len(pivot_mem.columns)))
ax.set_xticklabels(x_labels(pivot_mem.columns), fontsize=11)
ax.set_yticks(range(len(pivot_mem.index)))
ax.set_yticklabels([ALGO_LABELS[a] for a in pivot_mem.index], fontsize=11)
ax.set_xlabel("Quy mô mảng N", fontsize=12)
ax.set_title("Hình 9 – Heatmap: Bộ nhớ phụ (MB) – Algorithm × N", fontsize=13, pad=12)
plt.colorbar(im, ax=ax, label="Bộ nhớ phụ (MB)")
for r in range(pivot_mem.shape[0]):
    for c in range(pivot_mem.shape[1]):
        val = pivot_mem.values[r, c]
        if not np.isnan(val):
            txt = f"{val:.1f} MB"
            ax.text(c, r, txt, ha="center", va="center", fontsize=9,
                    color="black" if val < pivot_mem.values.max() * 0.6 else "white")
fig.tight_layout()
fig.savefig(os.path.join(OUT_DIR, "fig9_heatmap_memory.png"), dpi=150)
plt.close(fig)
print("✓ fig9_heatmap_memory.png")


# ══════════════════════════════════════════════════════════════════════════════
# BIỂU ĐỒ 10 – Tổng hợp 3 chiều (3-subplot): Thời gian / Bộ nhớ / Thao tác
# ══════════════════════════════════════════════════════════════════════════════
fig, axes = plt.subplots(1, 3, figsize=(17, 5))
fig.suptitle(
    "Hình 10 – Tổng hợp RQ1: Thời gian · Bộ nhớ phụ · Số thao tác theo N (D1 – ngẫu nhiên)",
    fontsize=13, y=1.01
)

# Subplot A: Thời gian (ms) trục log
ax = axes[0]
for algo in ALGOS:
    sub = df4[df4["algorithm"] == algo].sort_values("n")
    if sub.empty: continue
    ax.plot(range(len(sub["n"])), sub["median_ms"],
            label=ALGO_LABELS[algo], color=COLORS[algo],
            marker=MARKERS[algo], linewidth=2, markersize=6)
ax.set_yscale("log")
ax.set_xticks(range(len(SORTED_NS)))
ax.set_xticklabels(x_labels(SORTED_NS), fontsize=9, rotation=20, ha="right")
ax.set_xlabel("Quy mô N", fontsize=10)
ax.set_ylabel("Thời gian (ms, log)", fontsize=10)
ax.set_title("(a) Thời gian thực thi", fontsize=11)
ax.legend(fontsize=9)
ax.grid(axis="y", linestyle="--", alpha=0.4)

# Subplot B: Bộ nhớ phụ (MB)
ax = axes[1]
for algo in ALGOS:
    sub = df4[df4["algorithm"] == algo].sort_values("n")
    if sub.empty: continue
    ax.plot(range(len(sub["n"])), sub["aux_memory_mb"],
            label=ALGO_LABELS[algo], color=COLORS[algo],
            marker=MARKERS[algo], linewidth=2, markersize=6)
ax.set_xticks(range(len(SORTED_NS)))
ax.set_xticklabels(x_labels(SORTED_NS), fontsize=9, rotation=20, ha="right")
ax.set_xlabel("Quy mô N", fontsize=10)
ax.set_ylabel("Bộ nhớ phụ (MB)", fontsize=10)
ax.set_title("(b) Bộ nhớ phụ", fontsize=11)
ax.legend(fontsize=9)
ax.grid(axis="y", linestyle="--", alpha=0.4)

# Subplot C: Số thao tác (comparisons + radix ops), thang log
ax = axes[2]
for algo in CMP_ALGOS:
    sub = df4[df4["algorithm"] == algo].sort_values("n")
    sub = sub[sub["comparisons"].notna() & (sub["comparisons"] > 0)]
    if sub.empty: continue
    ax.plot(range(len(sub["n"])), sub["comparisons"],
            label=f"{ALGO_LABELS[algo]} (cmp)", color=COLORS[algo],
            marker=MARKERS[algo], linewidth=2, markersize=6)
sub_r = df4[df4["algorithm"] == "radix"].sort_values("n")
sub_r = sub_r[sub_r["operations"].notna() & (sub_r["operations"] > 0)]
ax.plot(range(len(sub_r["n"])), sub_r["operations"],
        label="Radix Sort (12N ops)", color=COLORS["radix"],
        marker=MARKERS["radix"], linewidth=2, markersize=6)
ax.set_yscale("log")
ax.set_xticks(range(len(SORTED_NS)))
ax.set_xticklabels(x_labels(SORTED_NS), fontsize=9, rotation=20, ha="right")
ax.set_xlabel("Quy mô N", fontsize=10)
ax.set_ylabel("Số thao tác (log)", fontsize=10)
ax.set_title("(c) Số thao tác", fontsize=11)
ax.legend(fontsize=9)
ax.yaxis.set_major_formatter(ticker.FuncFormatter(human_fmt))
ax.grid(axis="y", linestyle="--", alpha=0.4)

fig.tight_layout()
fig.savefig(os.path.join(OUT_DIR, "fig10_summary_3panel.png"), dpi=150, bbox_inches="tight")
plt.close(fig)
print("✓ fig10_summary_3panel.png")


# ══════════════════════════════════════════════════════════════════════════════
# BIỂU ĐỒ 11 – Tỷ lệ tăng tốc: Quick Sort / Radix Sort theo N
# ══════════════════════════════════════════════════════════════════════════════
fig, ax = plt.subplots(figsize=(10, 5))
speedup_ref = "quick"  # mốc so sánh
ref_data = df4[df4["algorithm"] == speedup_ref].sort_values("n").set_index("n")["median_ms"]

for algo in ALGOS:
    if algo == speedup_ref: continue
    sub = df4[df4["algorithm"] == algo].sort_values("n")
    speedups = []
    ns_ = []
    for _, row in sub.iterrows():
        nv = row["n"]
        if nv in ref_data.index and not pd.isna(ref_data[nv]) and not pd.isna(row["median_ms"]) and row["median_ms"] > 0:
            speedups.append(ref_data[nv] / row["median_ms"])
            ns_.append(nv)
    if ns_:
        ax.plot(range(len(ns_)), speedups,
                label=f"{ALGO_LABELS[algo]} (so với Quick Sort)",
                color=COLORS[algo], marker=MARKERS[algo],
                linewidth=2, markersize=7)

ax.axhline(1.0, color="gray", linestyle="--", linewidth=1, label="Mốc: Quick Sort = 1×")
x_pos = range(len(SORTED_NS))
ax.set_xticks(x_pos)
ax.set_xticklabels(x_labels(SORTED_NS), fontsize=11)
ax.set_xlabel("Quy mô mảng N", fontsize=12)
ax.set_ylabel("Tỷ lệ tăng tốc (lần)", fontsize=12)
ax.set_title("Hình 11 – Tỷ lệ tăng tốc so với Quick Sort", fontsize=13, pad=12)
ax.legend(fontsize=11)
ax.grid(axis="y", linestyle="--", alpha=0.4)
fig.tight_layout()
fig.savefig(os.path.join(OUT_DIR, "fig11_speedup_vs_quick.png"), dpi=150)
plt.close(fig)
print("✓ fig11_speedup_vs_quick.png")


# ══════════════════════════════════════════════════════════════════════════════
print(f"\n✅ Đã tạo xong {11} biểu đồ trong: {os.path.abspath(OUT_DIR)}")

