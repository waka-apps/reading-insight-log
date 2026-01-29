<script setup lang="ts">
type ReviewItem = {
  id: string;
  bookId: string;
  bookTitle: string;
  quote: string;
  interpretation: string;
  tags: string[];
  nextReviewAt: string;
};

const items = ref<ReviewItem[]>([
  {
    id: "ins-1",
    bookId: "book-1",
    bookTitle: "Effective Kotlin",
    quote: "All Kotlin programs start at the main function.",
    interpretation: "Kotlinのエントリポイントはmain。トップレベル関数として書けるのが特徴。",
    tags: ["kotlin", "entrypoint"],
    nextReviewAt: "2026-01-29",
  },
  {
    id: "ins-2",
    bookId: "book-2",
    bookTitle: "Clean Architecture",
    quote: "Dependencies should point inward.",
    interpretation: "依存の向きは内側（ドメイン）へ。外側は差し替え可能にする。",
    tags: ["architecture"],
    nextReviewAt: "2026-01-29",
  },
]);

const index = ref(0);

const current = computed(() => items.value[index.value] ?? null);
const isDone = computed(() => index.value >= items.value.length);

function answer(result: "REMEMBERED" | "UNCERTAIN") {
  if (!current.value) return;
  console.log({ insightId: current.value.id, result });
  index.value += 1;
}

function restart() {
  index.value = 0;
}
</script>

<template>
  <main class="p-6 max-w-3xl mx-auto">
    <header class="mb-6">
      <NuxtLink to="/books" class="underline opacity-80">← Back</NuxtLink>
      <h1 class="text-2xl font-bold mt-3">Review</h1>
      <p class="opacity-70 mt-1">Today’s review queue</p>
    </header>

    <section v-if="isDone" class="border rounded p-6">
      <p class="font-semibold">All done 🎉</p>
      <p class="opacity-70 mt-1">No more insights to review today.</p>
      <button class="mt-4 px-4 py-2 border rounded" @click="restart">Restart (dummy)</button>
    </section>

    <section v-else-if="current" class="border rounded p-6 space-y-4">
      <div class="flex items-start justify-between gap-4">
        <div>
          <div class="text-sm opacity-70">Book</div>
          <div class="font-semibold">{{ current.bookTitle }}</div>
        </div>
        <div class="text-sm opacity-70">Due: {{ current.nextReviewAt }}</div>
      </div>

      <div>
        <div class="text-sm opacity-70 mb-1">Quote</div>
        <div class="whitespace-pre-wrap">{{ current.quote }}</div>
      </div>

      <div>
        <div class="text-sm opacity-70 mb-1">Interpretation</div>
        <div class="whitespace-pre-wrap">{{ current.interpretation }}</div>
      </div>

      <div class="flex flex-wrap gap-2" v-if="current.tags.length">
        <span v-for="t in current.tags" :key="t" class="text-xs border rounded px-2 py-1 opacity-80">
          {{ t }}
        </span>
      </div>

      <div class="pt-2 flex gap-3">
        <button class="px-4 py-2 border rounded" @click="answer('REMEMBERED')">✅ 思い出せた</button>
        <button class="px-4 py-2 border rounded" @click="answer('UNCERTAIN')">❓ 怪しい</button>
      </div>

      <div class="text-sm opacity-60">{{ index + 1 }} / {{ items.length }}</div>
    </section>

    <section v-else class="border rounded p-6">
      <p>Loading...</p>
    </section>
  </main>
</template>
