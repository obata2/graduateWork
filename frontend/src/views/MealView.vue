<script setup>
const ingredients = [
  { name: 'うるち米(単一原料米,「コシヒカリ」)', amount: '200g' },
  { name: '牛肉(かた)', amount: '100g' },
  { name: '牛乳(店頭売り,紙容器入り)', amount: '100g' },
  { name: '鶏卵', amount: '60g' },
  { name: 'キャベツ', amount: '160g' },
  { name: 'もやし', amount: '70g' },
  { name: 'さつまいも', amount: '70g' },
  { name: 'にんじん', amount: '30g' },
  { name: 'えのきたけ', amount: '50g' },
  { name: 'えのきたけ', amount: '50g' }
]
</script>


<template>  
  <!-- 献立タブのメインコンテンツ -->
  <div class="px-4 sm:px-5 pt-4 pb-20">
    <!-- 考え事をしている女性のイラスト -->
    <div class="mb-6 sm:mb-8">
      <img
        src="https://api.builder.io/api/v1/image/assets/TEMP/27abdae8eff719dd46b7fac02a7f841726c1e352?width=244"
        alt="Character illustration"
        class="w-28 h-40 sm:w-30 sm:h-45 object-contain"
      />
    </div>

    <!-- 並び替え・オプション変更ボタン -->
    <div class="flex justify-between">
      <button class="flex items-center justify-center gap-1 px-3 py-2 rounded-xl min-h-[44px]">
        <span class="material-symbols-outlined">swap_horiz</span>
        <span class="text-label-large font-medium text-primary">並び替え</span>
      </button>

      <button class="flex items-center justify-center gap-1 px-3 py-2 rounded-xl min-h-[44px]">
        <span class="material-symbols-outlined">settings</span>
        <span class="text-label-large font-medium text-primary whitespace-nowrap">オプションを変更</span>
      </button>
    </div>

    <!-- 計算結果のカードカルーセル -->
    <div class="relative mb-6">      
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
              <span class="text-title-medium font-medium text-on-surface">○○○円</span>
            </div>
            <div class="flex items-center gap-2">
              <span class="text-label-large font-medium text-on-surface">総カロリー：</span>
              <span class="text-title-medium font-medium text-on-surface">○○○kcal</span>
            </div>
          </div>
          <div class="h-px bg-gray-300 my-2"></div>
          <p class="text-gray-600 text-label-small text-right text-on-surface">※食材のみの目安金額、生の状態での合計カロリーです</p>

          <!-- 食材リスト -->
          <div class="my-4">
            <div class="flex items-center gap-2 mb-4">
              <span class="material-symbols-outlined">list</span>
              <span class="text-label-large font-medium text-on-surface">食材</span>
            </div>

            <!-- 食材名とグラム数 -->
            <div class="space-y-1.5 h-60 text-label-large text-on-surface leading-5 flex-1 overflow-y-auto">
              <div v-for="ingredient in ingredients" :key="ingredient.name" class="flex justify-between items-start gap-4">
                <span class="text-left break-words ml-8">{{ ingredient.name }}</span>
                <span class="text-right break-words mr-8">{{ ingredient.amount }}</span>
              </div>
            </div>
          </div>

          <!-- カードのフッター -->
          <div class="flex justify-between items-center">
            <span class="text-label-large font-medium text-on-surface">1/80</span>
            <span class="material-symbols-outlined text-green-600">bar_chart_4_bars</span>
          </div>
        </div>
      </div>
    </div>

    <!-- レシピ生成ボタン -->
    <div class="flex justify-center">
      <button class="w-auto px-12 py-4 text-green-700 bg-green-100 shadow-md bg-inverse-on-surface text-primary text-title-medium font-medium rounded-2xl shadow-elevation-1 min-h-[56px]">
        レシピを生成する
      </button>
    </div>
  </div>
</template>