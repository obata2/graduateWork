<script setup>
import { onMounted, ref, nextTick } from 'vue';
import axios from "axios";

import PriceLatestTable from '../components/PriceLatestTable.vue';
import PriceTransitionGraph from '../components/PriceTransitionGraph.vue';
import ModalSquare from "C:\\Users\\81809\\Desktop\\demo\\frontend\\src\\components\\ModalSquare.vue";

// 上部のタブ切り替え用
const tabs = [
  { id: "tab1", label: "一覧表" },
  { id: "tab2", label: "推移グラフ" },
];
const activeTab = ref("tab1");

// 一覧表タブ用のデータの初期化処理
const originalTableData = ref([])
const editedTableData = ref([])
const isEditMode = ref(false)      // 価格を手入力するモードかのフラグ
const setTableData = async () => {
  const tableDataRes = await axios.get(`http://localhost:50000/estat/findAll`);
  originalTableData.value = tableDataRes.data;
}

// 推移グラフタブ用のデータの初期化処理
const nameAndIdMap = ref()      // セレクトボックス(食材)用の辞書
const ingredientName = ref()    // 選択状態にある、↑のkeyである食材名   (タイトル用にグラフへ渡す)
const priceUnitQtyMap = ref()   // 価格統計単位を示す辞書
const priceUnitQty = ref()      // 選択状態にある食材の、価格統計単位   (タイトル用にグラフへ渡す)
const setNameAndId = async () => {
  const mapperRes = await axios.get(`http://localhost:50000/mapper/mIngredients`);
  nameAndIdMap.value = mapperRes.data.nameAndId;
  priceUnitQtyMap.value = mapperRes.data.nameAndPriceUnitQty;
}
const areaParamMap = ref()      // セレクトボックス(比較する都市)用の辞書
const compCityName = ref()     // 選択状態にある、↑のkeyである都市名
const timeFromParamMap = ref()  // セレクトボックス(期間)用の辞書
//const timeFromParam = ref()   // 選択状態にある、cdTimeFrom
const setAPIParams = async () => {
  const apiParamsRes = await axios.get(`http://localhost:50000/estat/apiParams`);
  areaParamMap.value = apiParamsRes.data.areaParam;
  timeFromParamMap.value = apiParamsRes.data.timeFromParam;
}

// DOM生成時に変数の用意 
onMounted(async () => {
  await setTableData();
  await setNameAndId();
  await setAPIParams();
})

// DBを最新情報に更新し、さらにDBからデータを再取得する
const updateLatest = async () => {
  await axios.post("http://localhost:50000/estat/updateLatest", {
    cdArea: areaParamMap.value["名古屋市"],
    userId: "admin"
  });
  await setTableData();
}

// 表の編集を開始・キャンセルした時の挙動   (表作成コンポーネントに送るデータをoriginalのコピーにする)  
const startEdit = () => {
  editedTableData.value = structuredClone(originalTableData.value)
  isEditMode.value = true
}
const cancelEdit = () => {
  editedTableData.value = structuredClone(originalTableData.value)
  isEditMode.value = false
}

// 表の編集を完了した際に、DBの保存処理を行い、さらにDBからデータを再取得する
const saveEdits = async () => {
  await axios.post("http://localhost:50000/estat/saveEdits", editedTableData.value);
  console.log(editedTableData)
  isEditMode.value = false
  await setTableData();
}

// --- モーダルの表示まわり ---
const activeModal = ref(null); // 'beforeUpdate' | 'afterUpdate' | 'beforeEdit' | 'afterEdit' | null      <ModalSquare :show="activeModal === '○○'"に引っかかることで任意のモーダルを呼び出す  
const openModal = (name) => {
  activeModal.value = name;
};
const closeModal = () => {
  activeModal.value = null;         //<ModalSquare :show="activeModal === '○○'" のいずれにも引っかからなくなる
};

// 「グラフを描画」ボタンを押したときに、コンポーネントに渡すデータを整えて、描画まで行う
const chartRef = ref(null);
const isGraphVisible = ref(false);
const loading = ref(false);
const graphData = ref([]);
const renderChart = async () => {
  loading.value = true;
  const results = [];
  priceUnitQty.value = priceUnitQtyMap.value[ingredientName.value];
  //mainの都市のデータを追加
  /*const mainParams = {
    timeFrom: "2023000101",
    timeTo: "2025000505",
    areaCode: areaParamMap.value["名古屋市"]
  };*/
  const mainRes = await axios.post("http://localhost:50000/estat/fetch", {
    cdArea: areaParamMap.value["名古屋市"]
  });
  results.push({
    cityName: "名古屋市",
    dateLabel: mainRes.data.dateLabel[nameAndIdMap.value[ingredientName.value]],
    priceTransition: mainRes.data.priceTransition[nameAndIdMap.value[ingredientName.value]]
  });
  // compの都市が存在するならば追加
  if (compCityName.value !== "なし") {
    /*const compParams = {
      timeFrom: "2023000101",
      timeTo: "2025000505",
      areaCode: areaParam.value[compCity.value]
    };*/
    const compRes = await axios.post("http://localhost:50000/estat/fetch", {
      cdArea: areaParamMap.value[compCityName.value]
    });
    results.push({
      cityName: compCityName.value,
      dateLabel: compRes.data.dateLabel[nameAndIdMap.value[ingredientName.value]],
      priceTransition: compRes.data.priceTransition[nameAndIdMap.value[ingredientName.value]]
    });
  }
  graphData.value = results;
  //console.log(graphData.value);
  isGraphVisible.value = true;
  await nextTick();
  chartRef.value.renderChart();
  loading.value = false;
}
</script>

<template>
  <!-- データタブのメインコンテンツ -->
  <div class="w-full flex flex-col flex-1">
    <!-- 上部のタブ切り替え(共通) -->
    <div class="flex sticky top-20 mt-4 border-b bg-white">
      <button v-for="tab in tabs" :key="tab.id" @click="activeTab = tab.id"
        class="relative flex-1 text-center py-2 font-medium transition-colors duration-200">
        <!-- ラベル -->
        <span :class="activeTab === tab.id ? 'text-green-600' : 'text-gray-500'">
          {{ tab.label }}
        </span>
        <!-- 下線 -->
        <span class="absolute bottom-0 left-0 w-full h-0.5 transition-all duration-200"
          :class="activeTab === tab.id ? 'bg-green-600' : 'bg-transparent'"></span>
      </button>
    </div>

    <!-- タブ切り替えより下側 -->
    <div class="flex-1 px-4 pt-12 sm:px-5">
      <!-- 一覧表タブの中身 -->
      <div v-show="activeTab === 'tab1'">
        <h2 class="pb-1 font-semibold">
          食材価格表
        </h2>
        <div class="pb-6 text-sm text-gray-600">
          この表のデータを使用して、最適解が計算されます
        </div>
        <!-- 表本体 -->
        <PriceLatestTable :data="isEditMode ? editedTableData : originalTableData" :isEditMode="isEditMode">
        </PriceLatestTable>
        <!-- 更新ボタンと、手入力ボタン -->
        <div class="sticky bottom-20 px-4 pt-4 mt-8 -mx-4 bg-white shadow-[0_-0.125rem_0.25rem_-0_rgba(0,0,0,0.1)]">
          <!-- 編集モードではないとき -->
          <div v-if="!isEditMode" class="flex gap-3">
            <button class="flex w-full p-3 gap-2 justify-center border border-green-600 rounded-md"
              @click="openModal('beforeUpdate')">
              <span class="material-symbols-outlined text-green-600">cached</span>
              <p class="font-medium text-green-600">最新情報に更新</p>
            </button>
            <button class="flex w-full p-3 gap-2 justify-center bg-green-600 text-title-medium font-medium rounded-md"
              @click=startEdit>
              <span class="material-symbols-outlined text-white">edit_square</span>
              <p class="font-medium text-white">表を編集</p>
            </button>
          </div>
          <!-- 編集モードのとき -->
          <div v-else class="flex gap-3">
            <button class="flex p-3 gap-2 justify-center border border-gray-600 rounded-full" @click=cancelEdit>
              <span class="material-symbols-outlined text-gray-600">close</span>
              <p class="font-medium text-gray-600">キャンセル</p>
            </button>
            <button class="flex grow p-3 gap-2 justify-center bg-green-600 text-title-medium font-medium rounded-full"
              @click="() => {
                saveEdits();
                nextTick(() => openModal('afterEdit'));
              }">
              <span class="material-symbols-outlined text-white">check</span>
              <p class="font-medium text-white">保存</p>
            </button>
          </div>
        </div>
        <!-- 更新・編集を行う際のポップアップ(モーダル) -->
        <ModalSquare :show="activeModal === 'beforeUpdate'" width="80%" @close="closeModal">
          <div class="flex flex-col items-center text-center gap-6">
            <span
              class="material-symbols-outlined flex items-center justify-center w-12 h-12 rounded-full text-4xl text-yellow-600 bg-yellow-100">exclamation</span>
            <p class="text-sm">
              <span class="font-bold">固定</span>のチェックボックスが OFF になっている食材は、この処理によって<span
                class="font-bold text-red-500 underline">価格が上書き</span>されます。<br><br>
              <span class="text-base font-bold">更新してもよろしいですか？</span>
            </p>
            <div class="flex w-full gap-4">
              <button class="flex-1 p-2 border border-gray-600 text-gray-600 rounded-full"
                @click=closeModal>キャンセル</button>
              <button class="flex-1 p-2 bg-green-600 text-white rounded-full" @click="() => {
                updateLatest();
                nextTick(() => openModal('afterUpdate'));
              }">OK</button>
            </div>
          </div>
        </ModalSquare>
        <ModalSquare :show="activeModal === 'afterUpdate'" width="80%" @close="closeModal">
          <div class="flex flex-col items-center text-center gap-6">
            <span
              class="material-symbols-outlined flex items-center justify-center w-12 h-12 rounded-full text-4xl text-green-600 bg-green-100">check</span>
            <p class="text-lg font-bold">更新が完了しました!</p>
            <button class="w-full p-3 bg-green-600 text-white rounded-full" @click="closeModal">閉じる</button>
          </div>
        </ModalSquare>
        <ModalSquare :show="activeModal === 'afterEdit'" width="80%" @close="closeModal">
          <div class="flex flex-col items-center text-center gap-6">
            <span
              class="material-symbols-outlined flex items-center justify-center w-12 h-12 rounded-full text-4xl text-green-600 bg-green-100">check</span>
            <p class="text-lg font-bold">保存しました!</p>
            <button class="w-full p-3 bg-green-600 text-white rounded-full" @click="closeModal">閉じる</button>
          </div>
        </ModalSquare>
      </div>


      <!-- 推移グラフタブの中身 -->
      <div v-show="activeTab === 'tab2'">
        <!-- セレクトボックス -->
        <div class="grid grid-cols-10 gap-4">
          <div class="grid-item col-span-4">
            <label for="ingredient" class="block text-left text-xs font-medium mb-1">表示する食材</label>
            <select id="ingredient" v-model="ingredientName" class="block w-full text-sm rounded-md border-2 p-2
                     focus:outline-none focus:border-green-500 focus:ring-2 focus:ring-green-500">
              <option v-for="(value, key) in nameAndIdMap" :key="value" :value="key">
                {{ key }}
              </option>
            </select>
          </div>
          <div class="grid-item col-span-3">
            <label for="comparisionCity" class="block text-left text-xs font-medium mb-1">比較する都市</label>
            <select id="comparisionCity" v-model="compCityName" class="block w-full text-sm rounded-md border-2 p-2
                     focus:outline-none focus:border-green-500 focus:ring-2 focus:ring-green-500">
              <option v-for="(value, key) in areaParamMap" :key="value" :value="key">
                {{ key }}
              </option>
            </select>
          </div>
          <div class="grid-item col-span-3">
            <label class="block text-left text-xs font-medium mb-1">期間</label>
            <select class="block w-full text-sm rounded-md border-2 p-2
                     focus:outline-none focus:border-green-500 focus:ring-2 focus:ring-green-500">

            </select>
          </div>
        </div>
        <button
          class="w-full px-6 py-2 mt-6 mb-12 bg-gradient-to-r from-green-300 to-teal-300 text-title-medium font-medium rounded"
          @click="renderChart">グラフを描画</button>

        <!-- ローディング風のアニメーション -->
        <div v-if="loading" class="fixed inset-0 bg-green-600 bg-opacity-50 flex items-center justify-center z-50">
          <!-- Tailwindのスピナーアニメ -->
          <div class="flex space-x-2 items-center justify-center">
            <div class="w-4 h-4 bg-gray-50 rounded-full transform scale-75 animate-ping"></div>
            <div class="w-4 h-4 bg-gray-50 rounded-full transform scale-75 animate-ping" style="animation-delay: 0.1s;">
            </div>
            <div class="w-4 h-4 bg-gray-50 rounded-full transform scale-75 animate-ping" style="animation-delay: 0.2s;">
            </div>
          </div>
        </div>
        <!-- グラフの描画 -->
        <div v-if="isGraphVisible && graphData">
          <PriceTransitionGraph ref="chartRef" :graphData="graphData" :priceUnitQty="priceUnitQty"
            :ingredientName="ingredientName" class="h-auto p-4 bg-white rounded-xl border-2">
          </PriceTransitionGraph>
          <!-- 注意書き-->
          <p class=" text-xs text-gray-600 text-left p-4 ">
            このサービスは、政府統計総合窓口(e-Stat)のAPI機能を使用していますが、サービスの内容は国によって保証されたものではありません。</p>
        </div>
        <div v-else>
          <div
            className="w-full h-60 flex flex-col items-center justify-center text-center text-gray-400 bg-white rounded-xl p-4 border-2">
            <span class="material-symbols-outlined">chart_data</span>
            <p className="mt-4 font-medium">設定を選択して「グラフを描画」を押してください</p>
          </div>
        </div>

      </div>
    </div>
  </div>
</template>