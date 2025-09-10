<script setup>
import ilpResultList from "C:\\Users\\81809\\Desktop\\demo\\frontend\\src\\assets\\cachedILPResult.json";
const listSize = ilpResultList.length;
</script>


<template>  
  <!-- 献立タブのメインコンテンツ -->
  <div class="px-4 sm:px-5 pb-8">
    <!-- 考え事をしている女性のイラストと吹き出し -->
    <div class="flex items-center m-6 sm:mb-6 gap-6 border-black">
      <img
        src="@\assets\woman_thinking.svg"
        alt="woman_thinking"
        class="w-36 h-36 sm:w-30 sm:h-45 object-contain"
      />
      <!-- 吹き出し -->
      <svg xmlns='http://www.w3.org/2000/svg' width='300' height='90' viewBox='0 0 200 60' stroke-width="2" style='fill:#ffffff;stroke:#000000' ><!-- 背景色と枠線を指定 -->
        <path d='M15,6 A6 6 0 0 1 21,1 
                L170,1 A6,6 0 0 1 175,6 
                L175,42 A6,6 0 0 1 170,48 
                L21,48 A6,6 0 0 1 15,42 
                L15,40 L1,37 L15,25  Z'></path><!-- pathで角丸の吹き出しを描く -->
        <text x="95" y="30" text-anchor="middle" font-weight="bold" fill="#000000" stroke="none">今日の気分は...</text><!-- textで中央基点に文字の開始位置を指定 -->
      </svg>
    </div>

    <!-- 並び替え・絞り込み・オプション変更ボタン -->
    <div class="flex justify-between items-center w-full">
      <div class="flex gap-2">
        <button class="flex flex-col items-center justify-center gap-1 px-3 rounded-xl min-h-[44px]">
          <span class="material-symbols-outlined">swap_horiz</span>
          <span class="text-xs font-medium text-primary">並び替え</span>
        </button>
        <button class="flex flex-col items-center justify-center gap-1 px-3 rounded-xl min-h-[44px]">
          <span class="material-symbols-outlined">filter_list</span>
          <span class="text-xs font-medium text-primary">絞り込み</span>
        </button>
      </div>
      <button class="flex flex-col items-center justify-center gap-1 px-3 rounded-xl min-h-[44px]">
        <span class="material-symbols-outlined">settings</span>
        <span class="text-xs font-medium text-primary whitespace-nowrap">設定変更</span>
      </button>
    </div>

    <!-- 計算結果のカードカルーセル -->
    <div class="flex overflow-x-scroll snap-x gap-4 p-4">
      <div       
      v-for="iLPResult in ilpResultList"
      :key="iLPResult.id"
      class="flex-shrink-0 w-full snap-center">      
        <!-- 白色の角丸四角形 -->
        <div class="bg-white rounded-2xl shadow-md overflow-hidden flex flex-col">
        <!-- 上の緑の帯 -->
        <div class="h-2 bg-gradient-to-r from-green-500 to-green-200"></div>
          <!-- カードの中身全体 -->
          <div class="p-4">
            <!-- 合計金額と総カロリーの部分 -->
            <div class="flex justify-between">
              <div class="flex items-center gap-2">
                <span class="text-label-large font-medium text-on-surface">合計金額：</span>
                <span class="text-title-medium font-medium text-on-surface">{{iLPResult.totalPrice}}円</span>
              </div>
              <div class="flex items-center gap-2">
                <span class="text-label-large font-medium text-on-surface">総カロリー：</span>
                <span class="text-title-medium font-medium text-on-surface">{{iLPResult.totalKcal}}kcal</span>
              </div>
            </div>
            <div class="h-px bg-gray-300 my-2"></div>
          
            <!-- 食材リスト -->
            <div class="my-4">
              <div class="flex items-center gap-2 mb-4">
                <span class="material-symbols-outlined">list</span>
                <span class="text-label-large font-medium text-on-surface">食材</span>
              </div>
            
              <!-- 食材名とグラム数 -->
              <div class="space-y-1.5 h-60 text-label-large text-on-surface leading-5 flex-1 overflow-y-auto">
                <div v-for="(amount, name) in iLPResult.ingredients" :key="name" class="flex justify-between items-start gap-4">
                  <span class="text-left break-words ml-8">{{ name }}</span>
                  <span class="text-right break-words mr-8">{{ amount }}</span>
                </div>
              </div>
            </div>

            <!-- カードのフッター -->
            <div class="flex justify-between items-center">
              <span class="text-label-large font-medium text-on-surface">{{iLPResult.id}}/{{ listSize }}</span>
              <span class="material-symbols-outlined text-green-600">bar_chart_4_bars</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 注意書き-->
    <p class="text-gray-600 text-label-small text-left text-on-surface p-4 ">※食材のみの目安金額、生の状態での合計カロリーです</p>

    <!-- レシピ生成ボタン -->
    <div class="flex justify-center">
      <button class="w-auto px-12 py-4 text-green-700 bg-green-100 shadow-md bg-inverse-on-surface text-primary text-title-medium font-medium rounded-2xl shadow-elevation-1 min-h-[56px]">
        レシピを生成する
      </button>
    </div>
  </div>
</template>