import os
import pandas as pd
import matplotlib.pyplot as plt
import numpy as np

# Create output folder
out_dir = os.path.join("results", "figures")
os.makedirs(out_dir, exist_ok=True)

# Set high-quality style
plt.rcParams.update({
    'font.size': 11,
    'font.family': 'sans-serif',
    'axes.labelsize': 12,
    'axes.titlesize': 13,
    'xtick.labelsize': 10,
    'ytick.labelsize': 10,
    'legend.fontsize': 10,
    'figure.titlesize': 14,
    'figure.dpi': 300
})

colors = {
    'quick': '#1f77b4',
    'merge': '#ff7f0e',
    'heap': '#2ca02c',
    'radix': '#d62728',
    'pdq': '#9467bd',
    'lib': '#8c564b'
}

# -------------------------------------------------------------
# FIGURE 1: RQ1 - Scalability across N (10^5 to 2x10^7) on D1
# -------------------------------------------------------------
rq1_path = os.path.join("results", "rq1.csv")
if os.path.exists(rq1_path):
    df_rq1 = pd.read_csv(rq1_path)
    # Calculate median elapsed_ms for each (algorithm, n)
    df_rq1_med = df_rq1.groupby(['algorithm', 'n'])['elapsed_ms'].median().reset_index()
    
    fig, ax = plt.subplots(figsize=(7, 4.5))
    algos = ['radix', 'quick', 'merge', 'heap', 'lib']
    algo_labels = {
        'quick': 'Quick Sort',
        'merge': 'Merge Sort',
        'heap': 'Heap Sort (Person 3)',
        'radix': 'Radix Sort (Person 3)',
        'lib': 'lib-Arrays.sort'
    }
    
    for algo in algos:
        sub = df_rq1_med[df_rq1_med['algorithm'] == algo].sort_values('n')
        if not sub.empty:
            ax.plot(sub['n'], sub['elapsed_ms'], marker='o', linewidth=2, 
                    label=algo_labels.get(algo, algo), color=colors.get(algo, 'black'))
            
    ax.set_xscale('log')
    ax.set_yscale('log')
    ax.set_xlabel('Quy mô mảng N (phần tử, thang log)')
    ax.set_ylabel('Thời gian thực thi (ms, thang log)')
    ax.set_title('Hình 1: Đánh giá độ co giãn (RQ1) trên phân phối ngẫu nhiên D1')
    ax.grid(True, which="both", ls="--", alpha=0.5)
    ax.legend(frameon=True, loc='upper left')
    fig.tight_layout()
    fig1_file = os.path.join(out_dir, "fig1_rq1_scaling.png")
    fig.savefig(fig1_file)
    plt.close(fig)
    print("Generated:", fig1_file)

# -------------------------------------------------------------
# FIGURE 2: RQ2 - Execution time across D1-D5 at N = 10^6
# -------------------------------------------------------------
summary_path = os.path.join("results", "summary_all_v2.csv")
if os.path.exists(summary_path):
    df_sum = pd.read_csv(summary_path)
    # Filter N = 1000000
    df_1m = df_sum[df_sum['n'] == 1000000].copy()
    
    # Create combined dataset label: D1, D2, D3, D4, D5(k=1000), D5(k=10), D5(k=2)
    def label_data(row):
        if row['data'] == 'D5':
            return f"D5 (K={row['k']})"
        return row['data']
    
    df_1m['ds_label'] = df_1m.apply(label_data, axis=1)
    datasets = ['D1', 'D2', 'D3', 'D4', 'D5 (K=1000)', 'D5 (K=10)', 'D5 (K=2)']
    algos = ['radix', 'quick', 'merge', 'heap', 'pdq']
    algo_names = {
        'quick': 'Quick Sort',
        'merge': 'Merge Sort',
        'heap': 'Heap Sort',
        'radix': 'Radix Sort',
        'pdq': 'pdqsort'
    }
    
    x = np.arange(len(datasets))
    width = 0.15
    
    fig, ax = plt.subplots(figsize=(10, 5))
    for i, algo in enumerate(algos):
        vals = []
        for ds in datasets:
            match = df_1m[(df_1m['algorithm'] == algo) & (df_1m['ds_label'] == ds)]
            if not match.empty:
                vals.append(match['median_ms'].values[0])
            else:
                vals.append(0)
        ax.bar(x + (i - 2) * width, vals, width, label=algo_names.get(algo, algo),
               color=colors.get(algo, 'gray'))
        
    ax.set_xlabel('Họ phân phối dữ liệu (N = 1.000.000)')
    ax.set_ylabel('Thời gian thực thi trung vị (ms)')
    ax.set_title('Hình 2: Ảnh hưởng của dạng dữ liệu (RQ2) tới thời gian thực thi (N = 10^6)')
    ax.set_xticks(x)
    ax.set_xticklabels(datasets, rotation=15)
    ax.grid(axis='y', ls="--", alpha=0.5)
    ax.legend(frameon=True)
    fig.tight_layout()
    fig2_file = os.path.join(out_dir, "fig2_rq2_runtime_by_distribution.png")
    fig.savefig(fig2_file)
    plt.close(fig)
    print("Generated:", fig2_file)

# -------------------------------------------------------------
# FIGURE 3: RQ2 - Operations count across D1-D5 at N = 10^6
# -------------------------------------------------------------
    fig, ax = plt.subplots(figsize=(10, 5))
    # Comparison algorithms for ops
    algos_ops = ['radix', 'quick', 'merge', 'heap', 'pdq']
    for i, algo in enumerate(algos_ops):
        vals = []
        for ds in datasets:
            match = df_1m[(df_1m['algorithm'] == algo) & (df_1m['ds_label'] == ds)]
            if not match.empty:
                # convert to millions of ops
                vals.append(match['median_ops'].values[0] / 1_000_000.0)
            else:
                vals.append(0)
        ax.bar(x + (i - 2) * width, vals, width, label=algo_names.get(algo, algo),
               color=colors.get(algo, 'gray'))
        
    ax.set_xlabel('Họ phân phối dữ liệu (N = 1.000.000)')
    ax.set_ylabel('Số thao tác cơ bản (Triệu thao tác / ops)')
    ax.set_title('Hình 3: So sánh số thao tác cơ bản trên các phân phối dữ liệu (RQ2)')
    ax.set_xticks(x)
    ax.set_xticklabels(datasets, rotation=15)
    ax.grid(axis='y', ls="--", alpha=0.5)
    ax.legend(frameon=True)
    fig.tight_layout()
    fig3_file = os.path.join(out_dir, "fig3_rq2_ops_count.png")
    fig.savefig(fig3_file)
    plt.close(fig)
    print("Generated:", fig3_file)

# -------------------------------------------------------------
# FIGURE 4: RQ3 - pdqsort Ablation Study (Impact of Mechanisms)
# -------------------------------------------------------------
ablation_path = os.path.join("results", "pdq_ablation.csv")
if os.path.exists(ablation_path):
    df_ab = pd.read_csv(ablation_path)
    # Filter organ and D5-2
    fig, ax = plt.subplots(figsize=(8, 4.5))
    configs = ['core', 'no-ninther', 'no-break', 'no-heap']
    config_labels = ['Core pdqsort', 'Tắt Ninther', 'Tắt Phá Mẫu', 'Tắt Heap Fallback']
    
    # Let's inspect D5-2 runtime
    d5_data = df_ab[df_ab['data'] == 'D5-2']
    times = []
    for c in configs:
        match = d5_data[d5_data['config'] == c]
        if not match.empty:
            times.append(match['elapsed_ms'].values[0])
        else:
            times.append(0)
            
    bar_colors = ['#2ca02c', '#1f77b4', '#ff7f0e', '#d62728']
    bars = ax.bar(config_labels, times, color=bar_colors, width=0.5)
    
    for bar in bars:
        yval = bar.get_height()
        ax.text(bar.get_x() + bar.get_width()/2.0, yval + 10, f"{yval:.1f} ms", ha='center', va='bottom', fontweight='bold')
        
    ax.set_ylabel('Thời gian thực thi trên D5 (K=2) (ms)')
    ax.set_title('Hình 4: Thí nghiệm triệt tiêu (Ablation RQ3) - Tầm quan trọng của HeapSort Fallback')
    ax.grid(axis='y', ls="--", alpha=0.5)
    fig.tight_layout()
    fig4_file = os.path.join(out_dir, "fig4_rq3_pdq_ablation.png")
    fig.savefig(fig4_file)
    plt.close(fig)
    print("Generated:", fig4_file)

print("All 4 figures generated successfully!")
