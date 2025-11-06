<script setup>
import { defineProps, defineExpose, ref, computed } from 'vue'
import { Chart, registerables } from 'chart.js'

// Chart.jsの全コンポーネントを登録（スケール・軸などを含む）
Chart.register(...registerables)

const props = defineProps({
  graphData: {
    type: Array,
    default: () => []
  },
  priceUnit: {
    type: String
  },
  viewIngredient: {
    type: String
  }
})

// 明示的に親から呼べるように expose
defineExpose({ renderChart })

// --- 共通x軸を作る ---
const allLabels = computed(() => {
  const all = props.graphData.flatMap(d => d.dateLabel)
  // 重複を削除
  const unique = Array.from(new Set(all))
  // 年月を数値に変換してソート
  return unique.sort((a, b) => {
    const [yearA, monthA] = a.match(/(\d+)年(\d+)月/).slice(1).map(Number)
    const [yearB, monthB] = b.match(/(\d+)年(\d+)月/).slice(1).map(Number)
    if (yearA !== yearB) return yearA - yearB
    return monthA - monthB
  })
})
// --- 各都市のデータを共通x軸に合わせる ---
const alignData = (labels, values, allLabels) =>
  allLabels.map(label => {
    const idx = labels.indexOf(label)
    return idx !== -1 ? values[idx] : null
  })

const chartCanvas = ref(null)
let chartInstance = null


// グラフを初期化・再描画する関数
function renderChart () {
  if (!chartCanvas.value) return

  // 既にグラフが存在する場合は一旦破棄して再生成
  if (chartInstance) {
    chartInstance.destroy()
  }

  chartInstance = new Chart(chartCanvas.value, {
    type: 'line',
    data: {
      labels: allLabels.value,
      datasets: 
        props.graphData.map((d, i) => ({
          label: d.cityName,
          data: alignData(d.dateLabel, d.priceTransition, allLabels.value),
          borderColor: i === 0 ? "#3B82F6" : "#EF4444",
          tension: 0,
          pointRadius: 2
        }))
      
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: { display: true },
        tooltip: { mode: 'index', intersect: false }
      },
      scales: {
        y: {
          title: {
            display: true,
            text: '価格(円)'
          },
          beginAtZero: true
        }
      }
    }
  })
}

/* 初回描画
onMounted(() => {
  renderChart()
})

// graphDataが更新されたときにも再描画
watch(() => props.graphData, () => {
  renderChart()
}, { deep: true })
*/
</script>

<template>
  <div class="w-full pb-16" style="height: 350px;">
    <p>{{ props.viewIngredient }}の価格推移({{ props.priceUnit }}あたり)</p>
    <canvas ref="chartCanvas"></canvas>
  </div>
</template>