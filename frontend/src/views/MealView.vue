<script setup>
import { ref, onMounted, inject } from 'vue'
import axios from "axios"

const keywords = ref([
  { id: 1, text: '時短で料理'},
  { id: 2, text: '高たんぱく'},
  { id: 3, text: 'がっつり'},
  { id: 4, text: '和風'},
  { id: 5, text: '中華料理'},
  { id: 6, text: 'うどん'},
  { id: 7, text: 'スパゲッティ'},
])

const inputText = ref("");
const messages = ref([]);

// ILPで得た最適解をもっておく
const ILPResultList = ref([])
const loadInitial = async () => {
  const res = await axios.get(`http://localhost:50000/ILP/checkResult`);
  ILPResultList.value = res.data;
};
loadInitial();
// idの値でILPResultListの要素を検索する
const findById = (id) => {
  return ILPResultList.value.find(result => result.id === id);
};

// 画面リロード時、会話内容をsessionStorageから復元
onMounted(() => {
  const saved = sessionStorage.getItem("chat");
  if (saved) {
    messages.value = JSON.parse(saved);
  }
});

// sessionStorageに保存
const saveMessages = () => {
  sessionStorage.setItem("chat", JSON.stringify(messages.value));
};

// 送信処理
const sendMessage = async (keyWord) => {
  if (!keyWord.trim()) return;

  // ユーザーの発言追加
  messages.value.push({
    role: "user",
    text: keyWord
  });
  saveMessages();

  // 入力欄クリア
  inputText.value = "";

  // geminiに質問する版
  const reply = await geminiApi(keyWord)
  console.log(reply)
  messages.value.push({
    role: "assistant",
    text: reply.text,
    meals: Array.isArray(reply.meals) && reply.meals.length > 0
          ? reply.meals
          : null
  });
  saveMessages();
  
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
  */
  saveMessages();
};

// geminiに質問するAPI
const geminiApi = async (keyWord) => {
  const res =  await axios.post(`http://localhost:50000/chat/generateMeals`,
  { text: keyWord },
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

</script>

<template>

  <!-- レイアウト関係→それ自身のサイズ関係→文字のフォントや大きさ関係→背景・影関係→色関係→アニメーション・その他 -->

  <div class="px-4 pt-12 sm:px-5 flex flex-col">
    <!-- ヘッダー -->
    <h2 class="pb-6 font-semibold">
      キーワードをもとにして、AIが献立を生成します
    </h2>
    <!-- キーワードの例 -->
    <div class="flex flex-wrap gap-2 mb-6">
      <button
        v-for="keyword in keywords"
        :key="keyword.id"
        class="px-2 py-1 text-sm text-green-600 rounded-full border border-green-600"
        @click="sendMessage(keyword.text)"
      >
        {{ keyword.text }}
      </button>
    </div>

    <!-- チャットの表示エリア -->
    <div class="flex-1 overflow-y-auto space-y-2">
      <div
        v-for="(m, i) in messages"
        :key="i"
        class="flex"
        :class="m.role === 'user' ? 'justify-end' : 'justify-start'"
      >
        <!-- 吹き出しとその中身 -->
        <div
          class="px-3 py-2 rounded-lg text-sm"
          :class="m.role === 'user'
            ? 'bg-green-300 max-w-[80%]'
            : 'bg-gray-100 max-w-[80%]'"
        >
          {{ m.text }}
          <!-- meals が存在する場合だけ表示 -->
          <ul v-if="m.meals && m.meals.length" class="mt-3 space-y-2">
            <li
              v-for="(meal, idx) in m.meals"
              :key="idx"
              class="text-orange-600 underline cursor-pointer"
              @click="openFullScreen(
                'mealDetail', 
                {
                  ...meal,
                  ...findById(meal.selectedId)
                })"
            >
              {{ idx + 1 }}. {{ meal.menuName }}
            </li>
          </ul>
        </div>
      </div>
    </div>

    <!-- 設定ボタンとメッセージ入力欄 -->
    <div class="sticky flex bottom-20 left-0 right-0 py-4 gap-3 bg-gradient-to-t from-white via-white/100 to-transparent">
      <span class="material-symbols-outlined flex-col flex items-center justify-center rounded-full w-12 h-12 text-2xl bg-white border shadow-md">settings</span>
      <div class="relative flex-col grow">
        <input
          v-model="inputText"
          type="text"
          placeholder="キーワードを入力する"
          class="w-full h-12 px-6 pr-12 border rounded-full font-medium shadow-md placeholder:text-gray-300 focus:outline-none"
          @keyup.enter="sendMessage(inputText)"
        />
        <button 
          @click="sendMessage(inputText)"
          class="absolute right-3 top-1/2 -translate-y-1/2 w-6 h-6 flex items-center justify-center"
        >
          <span class="material-symbols-outlined bg-clip-text bg-gradient-to-r from-green-400 to-blue-400 text-transparent text-2xl">send</span>
        </button>
      </div>
    </div>
  </div>
</template>