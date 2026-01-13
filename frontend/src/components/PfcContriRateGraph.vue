<script setup>
import { onBeforeUnmount, ref, watch, nextTick } from 'vue';
import { Chart, BarController, BarElement, CategoryScale, LinearScale, Tooltip, Legend } from 'chart.js'

//import ModalSquare from 'C:\\Users\\81809\\Desktop\\demo\\frontend\\src\\components\\ModalSquare.vue';

// 登録
Chart.register(BarController, BarElement, CategoryScale, LinearScale, Tooltip, Legend)

const props = defineProps({
  graphData: { // 寄与率と目標値を渡す想定
    type: Object,
    default: () => ({
      contriRate: [],
    })
  }
});

//ラベル定義
const labels = ["たんぱく質のカロリー","脂質のカロリー","炭水化物のカロリー"];

const chartCanvas = ref(null);
let chartInstance = null;

// モーダルが開いた時(graphDataが渡されたタイミング)にグラフを描画・更新
watch(() => props.graphData, (newVal) => {
  if (newVal) {
    nextTick(() => {
      renderChart();
    });
  }
});

// グラフを描画する関数
function renderChart() {
  if (!chartCanvas.value) {
    return;
  }
  if (chartInstance) {
    chartInstance.destroy();
  }
  console.log("グラフを描画します");
  chartInstance = new Chart(chartCanvas.value, {
    type: 'bar',
    data: {
      labels: labels,
      datasets: [
          ...Object.entries(props.graphData).map(([ingredient, contriRate], idx) => ({
          label: ingredient,          // 凡例(食材名)
          data: contriRate,         // 寄与率の配列
          backgroundColor: `hsl(${idx * 100 % 360}, 60%, 60%)`, // 適当に色を振る
          stack: 'stack1'
        }))
      ]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,   // 親 div の高さに合わせる
      plugins: {
        legend: {
          position: 'top', // 下部に移動
          align: 'start',     // 左揃え
          labels: {
            boxWidth: 20,    // 凡例の色ボックスの幅
            padding: 10,     // 凡例項目間の余白
            font: {
              size: 12       // 文字サイズ
            }
          }
        },
        tooltip: { enabled: true }
      },
      scales: {
        x: { stacked: true }, // 寄与率は積み上げ
        y: { stacked: true, beginAtZero: true }
      }
    }
  });
}

onBeforeUnmount(() => {
  if (chartInstance) {
    chartInstance.destroy();
  }
});
</script>

<template>
  <!-- ModalSquare にスロットで渡すコンテンツ -->
   <div class="w-full" style="height: 450px;"> <!-- ←固定高さの親要素 -->
    <p class="text-lg mb-2 font-medium">pfc別のカロリーに対する、各食材の寄与率(%)</p>
    <canvas ref="chartCanvas"></canvas>
  </div>
</template>