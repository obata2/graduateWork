<script setup>
import { ref, onMounted, inject, nextTick } from 'vue'

import ModalSquare from "C:\\Users\\81809\\Desktop\\demo\\frontend\\src\\components\\ModalSquare.vue";
import ILPSolutionCarousel from 'C:\\Users\\81809\\Desktop\\demo\\frontend\\src\\components\\ILPSolutionCarousel.vue';
import { apiClient } from 'C:\\Users\\81809\\Desktop\\demo\\frontend\\src\\lib\\apiClient.js';

// ユーザー情報の取得
import { useUserStore } from 'C:\\Users\\81809\\Desktop\\demo\\frontend\\src\\stores\\users.js'
const userStore = useUserStore()
const userId = userStore.userId

const keywords = ref([
  { id: 1, text: '時短で料理' },
  { id: 2, text: '高たんぱく' },
  { id: 3, text: 'がっつり' },
  { id: 4, text: '和風' },
  { id: 5, text: '中華料理' },
  { id: 6, text: 'うどん' },
  { id: 7, text: 'スパゲッティ' },
])

const inputText = ref("");
const messages = ref([]);

// ILPで得た最適解をもっておく
const iLPResultList = ref([])
const loadInitial = async () => {
  const res = await apiClient.get(`/ilp-results/${userId}`);
  iLPResultList.value = res.data;
};
loadInitial();

// 画面リロード時、会話内容をsessionStorageから復元
onMounted(() => {
  const saved = loadFromSessionStorage("chat");
  if (saved) {
    messages.value = JSON.parse(saved);
  }
});

/* sessionStorageに保存
const saveMessages = () => {
  sessionStorage.setItem("chat", JSON.stringify(messages.value));
};*/
// sessionStorageに保存
const saveToSessionStorage = (key, value) => {
  sessionStorage.setItem(key, value);
}
// sessionStorageから読み込む
const loadFromSessionStorage = (key) => {
  return sessionStorage.getItem(key);
}

// 送信処理
const isWaitingGenerate = ref(false)
const sendMessage = async (keyWord) => {
  if (!keyWord.trim()) return;
  // ユーザーの発言追加
  messages.value.push({
    role: "user",
    text: keyWord
  });
  saveToSessionStorage("chat", JSON.stringify(messages.value));
  // 入力欄クリア
  inputText.value = "";
  // geminiに質問する
  isWaitingGenerate.value = true;
  const reply = await geminiApi(keyWord)
  messages.value.push({
    role: "assistant",
    text: reply.text,
    meals: Array.isArray(reply.meals) && reply.meals.length > 0
      ? reply.meals
      : null
  });
  saveToSessionStorage("chat", JSON.stringify(messages.value));
  isWaitingGenerate.value = false;
  /*
    // サンプルメッセージ版
    const reply = await sampleApi()
    console.log(reply)
    messages.value.push({
      role: "assistant",
      text: reply.text,
      meals: Array.isArray(reply.meals) && reply.meals.length > 0
            ? reply.meals
            : null
    });
  saveMessages();*/
};

// geminiに質問するAPI
const geminiApi = async (keyWord) => {
  const res = await apiClient.post(`/chat/generation/${userId}`,
    { text: keyWord},
    { headers: { 'Content-Type': 'application/json' } });
  return res.data
};

/* 確認用のサンプルAPIを呼び出す
const sampleApi = async () => {
  const res =  await axios.get(`http://localhost:50000/chat/sampleMessage`);
  return res.data
};
*/

// App.vue が提供した関数を受け取る
const openFullScreen = inject('openFullScreen')

// --- モーダルの表示まわり ---
const activeModal = ref(null); // 'showCandidate' | null      <ModalSquare :show="activeModal === '○○'"に引っかかることで任意のモーダルを呼び出す  
const openModal = (name) => {
  activeModal.value = name;
};
const closeModal = () => {
  activeModal.value = null;         //<ModalSquare :show="activeModal === '○○'" のいずれにも引っかからなくなる
};
const activeModalTab = ref('specify');

const isWaitingILP = ref(false)
// 食材指定・除外用の名前リスト
const ingNameList = ref(null)
const selectedIngredients = ref([]);
const excludedIngredients = ref([]);
const mode = ref('select')
const getIngNameList = async () => {
  const res = await apiClient.get('/nutrient/nameList');
  ingNameList.value = res.data
}
getIngNameList();
// チェックボックスの選択をクリアする
const clearSelection = () => {
  if (mode.value === "select") {
    selectedIngredients.value = [];
  } else {
    excludedIngredients.value = [];
  }
};
// チェックボックスに入力があった際に、selected または excluded への追加を排他的になるよう行う
const toggleItem = (item, checked) => {
  if (checked) {
    if (mode.value === "select") {
      if (!selectedIngredients.value.includes(item)) {
        selectedIngredients.value.push(item);
      }
      excludedIngredients.value = excludedIngredients.value.filter(v => v !== item);
    } else {
      if (!excludedIngredients.value.includes(item)) {
        excludedIngredients.value.push(item);
      }
      selectedIngredients.value = selectedIngredients.value.filter(v => v !== item);
    }
  } else {
    selectedIngredients.value = selectedIngredients.value.filter(v => v !== item);
    excludedIngredients.value = excludedIngredients.value.filter(v => v !== item);
  }
};
// チェックボックスのon off 判定用
const isSelected = (item) => selectedIngredients.value.includes(item);
const isExcluded = (item) => excludedIngredients.value.includes(item);
// 指定・除外を反映させて、数理最適化を行い、さらにDBを更新する
const replace = async () => {
  isWaitingILP.value = true;
  await apiClient.put(`/ilp-results/${userId}`, {
    selected: selectedIngredients.value,
    excluded: excludedIngredients.value
  });
  loadInitial();
  isWaitingILP.value = false;
}
// 配列の各要素をつなげた文字列を作る
const formatArray = (arr) => {
  return arr.join(",");
}
// localStorageに保存
const saveToLocalStorage = (key, value) =>{
  localStorage.setItem(key, value);
}
// localStorageから読み込む
const loadFromLocalStorage = (key) => {
  return localStorage.getItem(key);
}

</script>

<template>

  <!-- レイアウト関係→それ自身のサイズ関係→文字のフォントや大きさ関係→背景・影関係→色関係→アニメーション・その他 -->

  <div class="w-full px-4 pt-12 sm:px-5 flex flex-col">
    <!-- ヘッダー -->
    <h2 class="pb-6 font-semibold">
      キーワードをもとにして、AIが献立を生成します
    </h2>
    <!-- キーワードの例 -->
    <div class="flex flex-wrap gap-2 mb-6">
      <button v-for="keyword in keywords" :key="keyword.id"
        class="px-2 py-1 text-sm text-green-600 rounded-full border border-green-600"
        @click="sendMessage(keyword.text)">
        {{ keyword.text }}
      </button>
    </div>

    <!-- チャットの表示エリア -->
    <div class="flex-1 overflow-y-auto space-y-2">
      <div v-for="(m, i) in messages" :key="i" class="flex"
        :class="m.role === 'user' ? 'justify-end' : 'justify-start'">
        <!-- 吹き出しとその中身 -->
        <div class="px-3 py-2 rounded-lg text-sm" :class="m.role === 'user'
          ? 'bg-green-300 max-w-[80%]'
          : 'bg-gray-100 max-w-[80%]'">
          {{ m.text }}
          <!-- meals が存在する場合だけ表示 -->
          <ul v-if="m.meals && m.meals.length" class="mt-3 space-y-2">
            <li v-for="(meal, idx) in m.meals" :key="idx" class="text-orange-600 underline cursor-pointer" @click="openFullScreen(
              'mealDetail',
              {
                userId: userId,
                ...meal
              })">
              {{ idx + 1 }}. {{ meal.menuName }}
            </li>
          </ul>
        </div>
      </div>
      <!-- 生成待ちのアニメーションの吹き出し -->
      <div v-if="isWaitingGenerate" class="flex justify-start items-center gap-2 px-3 py-2 rounded-lg text-sm bg-gray-100 w-fit">
        <div class="animate-spin h-4 w-4 bg-gray-400 rounded"></div>
        <p>生成中です...</p>
      </div>
    </div>

    <!-- 設定ボタンとメッセージ入力欄 -->
    <div
      class="sticky flex bottom-20 left-0 right-0 py-4 gap-3 bg-gradient-to-t from-white via-white/100 to-transparent">
      <button class="flex flex-col  items-center justify-center rounded-full w-14 h-14 border shadow-md bg-white" @click="
        openModal('showCandidate')">
        <span class="material-symbols-outlined text-2xl text-gray-500">grocery</span>
        <p class="text-xs text-gray-500 -mt-1">食材</p>
      </button>
      <div class="relative flex-col grow">
        <input v-model="inputText" type="text" placeholder="キーワードを入力する"
          class="w-full h-14 px-6 pr-12 border rounded-full font-medium shadow-md placeholder:text-gray-300 focus:outline-none"
          @keyup.enter="sendMessage(inputText)" />
        <button @click="sendMessage(inputText)"
          class="absolute right-3 top-1/2 -translate-y-1/2 w-8 h-8 flex items-center justify-center">
          <span
            class="material-symbols-outlined bg-clip-text bg-gradient-to-r from-green-400 to-blue-400 text-transparent text-3xl">send</span>
        </button>
      </div>
    </div>

    <!-- 外部知識の確認と、食材指定のモーダル -->
    <ModalSquare :show="activeModal === 'showCandidate'" width="100%" height="80%" position="bottom" :animation=true
      @close="closeModal">
      <!-- タブ切り替え -->
      <div class="flex p-1 rounded-lg my-4 bg-gray-200 overflow-hidden">
        <button @click="activeModalTab = 'specify'"
          :class="['flex-1 py-2 rounded-lg text-xs font-bold transition-all', activeModalTab === 'specify' ? 'bg-green-600 text-white shadow-sm' : 'text-gray-500']">
          食材オプション
        </button>
        <button @click="activeModalTab = 'solutionList'"
          :class="['flex-1 py-2 rounded-lg text-xs font-bold transition-all', activeModalTab === 'solutionList' ? 'bg-green-600 text-white shadow-sm' : 'text-gray-500']">
          候補リスト
        </button>
      </div>
      <!-- 「食材オプション」の中身 -->
      <div v-show="activeModalTab === 'specify'">
        <p class="mb-2 font-medium text-sm text-gray-600">指定する・除外する食材を選択してください</p>
        <p class="mb-4 text-xs text-gray-600 underline">※選択が多すぎると、計算が上手くいかない場合があります</p>
        <!-- モード切替 -->
        <div class="mb-4 flex gap-2 text-sm">
          <button class="px-3 py-1 border rounded" :class="mode === 'select' && 'bg-blue-100'" @click="mode = 'select'">
            指定
          </button>
          <button class="px-3 py-1 border rounded" :class="mode === 'exclude' && 'bg-red-100'"
            @click="mode = 'exclude'">
            除外
          </button>
        </div>
        <!-- チェックボックス -->
        <div class="grid grid-cols-3 gap-2 mb-4">
          <label v-for="ingName in ingNameList" :key="ingName" class="flex items-center gap-2" :class="[
            isSelected(ingName) && 'text-blue-600',
            isExcluded(ingName) && 'text-red-600'
          ]">
            <input type="checkbox" :checked="isSelected(ingName) || isExcluded(ingName)"
              @change="toggleItem(ingName, $event.target.checked)" :class="[
                isSelected(ingName) && 'accent-blue-500',
                isExcluded(ingName) && 'accent-red-500'
              ]" />
            <span class="text-sm">{{ ingName }}</span>
          </label>
        </div>
        <div class="flex gap-4">
          <button class="flex-1 p-2 border border-gray-600 text-gray-600 rounded-lg"
            @click="clearSelection">選択をクリア</button>
          <button class="flex-1 p-2 bg-green-600 text-white rounded-lg"
            @click="openModal('beforeReplace')">この設定で計算</button>
        </div>
      </div>
      <!-- 「候補リスト」の中身 -->
      <div v-show="activeModalTab === 'solutionList'">
        <p class="font-medium text-sm text-gray-600">AIが献立を生成する際は、下記のリストを参照しています</p>
        <ILPSolutionCarousel :data="iLPResultList"></ILPSolutionCarousel>
        <div class="mt-4 text-sm text-gray-600">指定した食材：{{ loadFromLocalStorage('selected') }}</div>
        <div class="mt-4 text-sm text-gray-600">除外した食材：{{ loadFromLocalStorage('excluded') }}</div>
      </div>
    </ModalSquare>

    <!-- 最適化の計算を行う前の、確認画面用のモーダル -->
    <ModalSquare :show="activeModal === 'beforeReplace'" width="90%" @close="openModal('showCandidate')">
      <div class="flex flex-col items-center text-center gap-6">
        <span
          class="material-symbols-outlined flex items-center justify-center w-12 h-12 rounded-full text-4xl text-yellow-600 bg-yellow-100">exclamation</span>
        <div class="flex flex-col gap-2 text-sm">
          <p>以下の設定で、食材の組み合わせを最適化します</p>
          <p class="text-left font-semibold">指定する：<br><span class="font-normal">{{ formatArray(selectedIngredients) }}</span></p>
          <p class="text-left font-semibold">除外する：<br><span class="font-normal">{{ formatArray(excludedIngredients) }}</span></p>
          <span class="text-xs text-gray-600 underline">※この処理には20秒ほど時間がかかることがあります</span>
        </div>
        <div class="flex w-full gap-4">
          <button class="flex-1 p-2 border border-gray-600 text-gray-600 rounded-full" @click="
            openModal('showCandidate')">戻る</button>
          <button class="flex-1 p-2 bg-green-600 text-white rounded-full"
            @click="() => {
              isWaitingILP = true;
              openModal('afterReplace');
              nextTick(() => replace())}">計算する</button>
        </div>
      </div>
    </ModalSquare>

    <!-- 計算待ち・計算完了後のモーダル -->
    <ModalSquare :show="activeModal === 'afterReplace'" width="90%" :closable="!isWaitingILP" @close="openModal('showCandidate')">
      <div v-if="isWaitingILP">
        <div class="flex justify-center items-center gap-4 pb-4" aria-label="読み込み中">
          <div class="animate-spin h-6 w-6 border-4 border-green-500 rounded-full border-t-transparent"></div>
          <p class="text-lg font-semibold">計算中です...</p>
        </div>
        <p class="text-center text-gray-600">しばらくお待ちください</p>
      </div>
      <div v-else>
        <div class="flex flex-col items-center text-center gap-6">
          <span class="material-symbols-outlined flex items-center justify-center w-12 h-12 rounded-full text-4xl text-green-600 bg-green-100">check</span>
          <p class="text-lg font-bold">完了しました!</p>
          <p class="text-gray-600">「候補リスト」から、結果を確認してください</p>
          <button class="w-full p-3 bg-green-600 text-white rounded-full" 
            @click="() => {
              openModal('showCandidate')
              saveToLocalStorage('selected', formatArray(selectedIngredients))
              saveToLocalStorage('excluded', formatArray(excludedIngredients))}">閉じる</button>
        </div>
      </div>
    </ModalSquare>
  </div>
</template>