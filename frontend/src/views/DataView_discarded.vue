<script setup>
import { nextTick, ref } from 'vue';
import axios from "axios";

import PriceTransitionGraph from '../components/PriceTransitionGraph.vue';
//import PriceLatestTable from '../components/PriceLatestTable.vue';

//住んでいる地域の設定
const mainCity = ref("名古屋市");
//セレクトボックス用のリスト
const ingredientsList = ref([]);
const mainCityList = ref([]);
const compCityList = ref([]);
//APIパラメータと紐づけるための辞書たち
const nameAndId = ref(null);
const areaParam = ref(null);
const timeFromParam = ref(null);
const timeToParam = ref(null);
//セレクトボックスで選択されたものを入れておく変数(v-modelでバインディング)
const ingredient = ref();
const compCity = ref();
//一覧タブ用の変数
const mainData = ref();


const priceUnitList = ref();
const priceUnitQty = ref();
const viewIngredient = ref();

const isTableVisible = ref(false)


//nameAndIdの要素(value つまりid)1つ1つに対して、{mainDataの priceTransition[id]の最後尾, dateLabel[id]の最後尾, priceUnit[id]}の3種類のデータを表にする


const setAPIParams = async () => {
  const res = await axios.get(`http://localhost:50000/estat/params`);    //戻りがpromiseであることに注意
  areaParam.value = res.data.areaParam;
  timeFromParam.value = res.data.timeFromParam;
  timeToParam.value = res.data.timeToParam;
}

const setIngredientsList = async () => {
  const res = await axios.get(`http://localhost:50000/filter/filteredIngAndName`);    //戻りがpromiseであることに注意
  nameAndId.value = res.data;
}

const setMainData = async () => {
  // mainの都市
  const mainParams = {
    timeFrom: "2023000101",
    timeTo: "2025000505",
    areaCode: areaParam.value[mainCity.value]
  };
  const mainRes = await axios.post("http://localhost:50000/estat/download", mainParams);
  mainData.value = {
    cityName: mainCity.value,
    dateLabel: mainRes.data.dateLabel,
    priceTransition: mainRes.data.priceTransition
  };
}

const setPriceUnit = async () => {
  const priceUnitRes = await axios.get(`http://localhost:50000/filter/idAndPriceUnit`);
  priceUnitList.value = priceUnitRes.data;
}

//変数の初期化処理
const tableData = ref([])
const setTableData = async () => {
  const tableDataRes = await axios.get(`http://localhost:50000/estat/findAll`);
  tableData.value = tableDataRes.data;
}
//テーブル用のデータ
const init = async () => {
  await setAPIParams();
  mainCityList.value = Object.keys(areaParam.value);
  compCityList.value = ["なし", ...mainCityList.value];
  compCity.value = compCityList.value[0];
  await setIngredientsList();
  ingredientsList.value = Object.keys(nameAndId.value);
  ingredient.value = ingredientsList.value[0];
  await setPriceUnit();
  await setMainData();
  isTableVisible.value = true

  setTableData();
}
init();


const chartRef = ref(null);
const isGraphVisible = ref(false);
const loading = ref(false);
const graphData = ref([]);

//描画ボタンが押された時、グラフに渡すデータを揃えて、描画する
const fetchEstat = async () => {
  const ingId = nameAndId.value[ingredient.value];
  priceUnitQty.value = priceUnitList.value[ingId];
  loading.value = true;
  viewIngredient.value = ingredient.value;
  const results = [];
  //mainの都市のデータを取り出す
  results.push({
    cityName: mainCity.value,
    dateLabel: mainData.value.dateLabel[ingId],
    priceTransition: mainData.value.priceTransition[ingId]
  });
  // compの都市が存在するならば追加
  if(compCity.value !== "なし"){
    const compParams = {
      timeFrom: "2023000101",
      timeTo: "2025000505",
      areaCode: areaParam.value[compCity.value]
    };
    const compRes = await axios.post("http://localhost:50000/estat/download", compParams);
    results.push({
      cityName: compCity.value,
      dateLabel: compRes.data.dateLabel[ingId],
      priceTransition: compRes.data.priceTransition[ingId]
    });
  }

  // ---- Chart.jsに渡す用データ ----
  graphData.value = results;
  console.log(graphData.value);
  isGraphVisible.value = true;

  await nextTick();
  chartRef.value.renderChart();
  loading.value = false;
}

const tabs = [
  { id: "tab1", label: "一覧" },
  { id: "tab2", label: "推移" },
];

const activeTab = ref("tab1");

</script>


<template>  
  <!-- データタブのメインコンテンツ -->
  
  
  <div class="flex flex-col flex-1 w-full">
    <!-- 上部のタブ切り替え(共通) -->
    <div class="flex sticky top-20 mt-4 border-b bg-white">
      <button
        v-for="tab in tabs"
        :key="tab.id"
        @click="activeTab = tab.id"
        class="relative flex-1 text-center py-2 font-medium transition-colors duration-200"
      >
        <!-- ラベル -->
        <span :class="activeTab === tab.id ? 'text-green-600' : 'text-gray-500'">
          {{ tab.label }}
        </span>
        <!-- 下線 -->
        <span
          class="absolute bottom-0 left-0 w-full h-0.5 transition-all duration-200"
          :class="activeTab === tab.id ? 'bg-green-600' : 'bg-transparent'"
        ></span>
      </button>
    </div>
    

    <div class="flex-1 px-3 py-12 sm:px-5">
    <!-- 一覧タブの中身 -->
    <div v-show="activeTab === 'tab1'">
      <div v-if="isTableVisible">
        {{ tableData }}
      </div>
    </div>

    <!-- 推移タブの中身 -->
    <div v-show="activeTab === 'tab2'">
    <!-- セレクトボックス -->
    <div class="grid grid-cols-10 gap-4">
      <div class="grid-item col-span-4">
        <label for="ingredient" class="block text-left text-xs font-medium mb-1">表示する食材</label>
        <select
          id="ingredient"
          v-model="ingredient"
          class="block w-full text-sm rounded-md border-2 p-2
                 focus:outline-none focus:border-green-500 focus:ring-2 focus:ring-green-500"
        >
          <option
            v-for="(item, index) in ingredientsList"
            :key="index"
            :value="item"
          >
            {{ item }}
          </option>
        </select>
      </div>
      <div class="grid-item col-span-3">
        <label for="comparisionCity" class="block text-left text-xs font-medium mb-1">比較する都市</label>
        <select
          id="comparisionCity"
          v-model="compCity"
          class="block w-full text-sm rounded-md border-2 p-2
                 focus:outline-none focus:border-green-500 focus:ring-2 focus:ring-green-500"
        >
          <option
            v-for="(item, index) in compCityList"
            :key="index"
            :value="item"
          >
            {{ item }}
          </option>
        </select>
      </div>
      <div class="grid-item col-span-3">
        <label class="block text-left text-xs font-medium mb-1">期間</label>
        <select

          class="block w-full text-sm rounded-md border-2 p-2
                 focus:outline-none focus:border-green-500 focus:ring-2 focus:ring-green-500"
        >
  
        </select>
      </div>
    </div>

    <button class="w-full px-6 py-2 mt-6 mb-12 bg-gradient-to-r from-green-300 to-teal-300 text-title-medium font-medium rounded"
            @click="fetchEstat">
      グラフを描画
    </button>


    <!-- ローディング風のアニメーション -->
    <div v-if="loading" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <!-- Tailwindのスピナーアニメ -->
      <div class="flex space-x-2 items-center justify-center">
        <div class="w-4 h-4 bg-gray-50 rounded-full transform scale-75 animate-ping"></div>
        <div class="w-4 h-4 bg-gray-50 rounded-full transform scale-75 animate-ping" style="animation-delay: 0.1s;"></div>
        <div class="w-4 h-4 bg-gray-50 rounded-full transform scale-75 animate-ping" style="animation-delay: 0.2s;"></div>
      </div>
    </div>
    <!-- グラフの描画 -->
    <div v-if="isGraphVisible && graphData">
      <PriceTransitionGraph
        ref="chartRef"
        :graphData="graphData"
        :priceUnit="priceUnitQty"
        :viewIngredient="viewIngredient"
        class="h-auto p-4 bg-white rounded-xl border-2">
      </PriceTransitionGraph>
      <!-- 注意書き-->
      <p class=" text-xs text-gray-600 text-left p-4 ">このサービスは、政府統計総合窓口(e-Stat)のAPI機能を使用していますが、サービスの内容は国によって保証されたものではありません。</p>
    </div>
    <div v-else>
      <div className="w-full h-60 flex flex-col items-center justify-center text-center text-gray-400 bg-white rounded-xl p-4 border-2">
        <span class="material-symbols-outlined">chart_data</span>
        <p className="mt-4 font-medium">設定を選択して「グラフを描画」を押してください</p>
      </div>
    </div>
  </div>
  </div>

    
  </div>
</template>