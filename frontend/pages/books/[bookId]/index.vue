<script setup lang="ts">
import type { Insight } from "~/types/insight";

const route = useRoute();
const bookId = computed(() => String(route.params.bookId));

const api = useApi();

const pending = ref(true);
const error = ref<string | null>(null);
const insights = ref<Insight[]>([]);

async function load() {
  pending.value = true;
  error.value = null;
  try {
    insights.value = await api.get<Insight[]>(`/admin/books/${bookId.value}/insights`);
  } catch (e) {
    error.value = (e as Error).message;
  } finally {
    pending.value = false;
  }
}

onMounted(load);
watch(bookId, load); // bookIdが変わったら再取得
</script>

<template>
  <main class="p-6 max-w-3xl mx-auto">
    <header class="mb-6">
      <NuxtLink to="/books" class="underline opacity-80">← Back</NuxtLink>
      <h1 class="text-2xl font-bold mt-3">Book Detail</h1>
      <p class="opacity-80 mt-1">bookId: {{ bookId }}</p>
    </header>

    <section class="border rounded p-4">
      <div class="flex items-center justify-between gap-3">
        <p class="opacity-70">Insights</p>
        <NuxtLink :to="`/books/${bookId}/new-insight`" class="px-3 py-2 rounded border inline-block">
          + New Insight
        </NuxtLink>
      </div>

      <div class="mt-4">
        <p v-if="pending" class="opacity-70">Loading...</p>
        <p v-else-if="error" class="text-red-600">{{ error }}</p>
        <p v-else-if="insights.length === 0" class="opacity-70">No insights yet.</p>

        <ul v-else class="space-y-3">
          <li v-for="i in insights" :key="i.id" class="border rounded p-3">
            <div class="text-sm opacity-70">
              <span>createdAt: {{ i.createdAt }}</span>
              <span class="ml-3">nextReviewAt: {{ i.nextReviewAt }}</span>
              <span class="ml-3">interval: {{ i.reviewIntervalDays }}d</span>
            </div>

            <div class="mt-2">
              <p class="font-semibold">Quote</p>
              <p class="whitespace-pre-wrap">{{ i.quote }}</p>
            </div>

            <div class="mt-2">
              <p class="font-semibold">Interpretation</p>
              <p class="whitespace-pre-wrap">{{ i.interpretation }}</p>
            </div>

            <div v-if="i.tags?.length" class="mt-2 flex flex-wrap gap-2">
              <span v-for="t in i.tags" :key="t" class="text-xs border rounded px-2 py-1 opacity-80">
                {{ t }}
              </span>
            </div>
          </li>
        </ul>
      </div>
    </section>
  </main>
</template>
