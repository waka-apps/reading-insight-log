<script setup lang="ts">
import type { Insight } from "~/types/insight";
import type { ReviewRequest } from "~/types/review";

const api = useApi();

const pending = ref(false);
const error = ref<string | null>(null);

const due = ref<Insight[]>([]);
const index = ref(0);

const current = computed(() => due.value[index.value] ?? null);
const isDone = computed(() => !pending.value && current.value == null);

async function fetchDue() {
  pending.value = true;
  error.value = null;
  try {
    due.value = await api.get<Insight[]>("/admin/review/today");
    index.value = 0;
  } catch (e) {
    error.value = (e as Error).message;
  } finally {
    pending.value = false;
  }
}

onMounted(fetchDue);

async function onAnswer(result: "REMEMBERED" | "UNCERTAIN") {
  const cur = current.value;
  if (!cur) return;

  pending.value = true;
  error.value = null;

  try {
    const body: ReviewRequest = { result };
    await api.post<ReviewRequest, Insight>(`/admin/insights/${cur.id}/review`, body);

    // その場で消す（次の要素が詰められるので index は据え置きでOK）
    due.value = due.value.filter((x) => x.id !== cur.id);
    // index.value はそのまま
  } catch (e) {
    error.value = (e as Error).message;
  } finally {
    pending.value = false;
  }
}
</script>

<template>
  <main class="p-6 max-w-3xl mx-auto">
    <header class="mb-6">
      <NuxtLink to="/books" class="underline opacity-80">← Back</NuxtLink>
      <h1 class="text-2xl font-bold mt-3">Review</h1>
      <p class="opacity-70 mt-1">Today’s review queue</p>
    </header>

    <section v-if="error" class="border rounded p-4 mb-4">
      <p class="font-semibold">Error</p>
      <p class="opacity-80 mt-1">{{ error }}</p>
      <button class="mt-3 px-4 py-2 border rounded" :disabled="pending" @click="fetchDue">Retry</button>
    </section>

    <section v-if="pending && !current" class="border rounded p-6">
      <p>Loading...</p>
    </section>

    <section v-else-if="isDone" class="border rounded p-6">
      <p class="font-semibold">All done 🎉</p>
      <p class="opacity-70 mt-1">No more insights to review today.</p>
      <button class="mt-4 px-4 py-2 border rounded" :disabled="pending" @click="fetchDue">Reload</button>
    </section>

    <section v-else class="border rounded p-6 space-y-4">
      <div class="flex items-start justify-between gap-4">
        <div>
          <div class="text-sm opacity-70">Book</div>
          <div class="font-semibold">bookId: {{ current!.bookId }}</div>
        </div>
        <div class="text-sm opacity-70">Due: {{ current!.nextReviewAt }}</div>
      </div>

      <div>
        <div class="text-sm opacity-70 mb-1">Quote</div>
        <div class="whitespace-pre-wrap">{{ current!.quote }}</div>
      </div>

      <div>
        <div class="text-sm opacity-70 mb-1">Interpretation</div>
        <div class="whitespace-pre-wrap">{{ current!.interpretation }}</div>
      </div>

      <div class="flex flex-wrap gap-2" v-if="current!.tags?.length">
        <span v-for="t in current!.tags" :key="t" class="text-xs border rounded px-2 py-1 opacity-80">
          {{ t }}
        </span>
      </div>

      <div class="pt-2 flex gap-3">
        <button
          class="px-4 py-2 border rounded disabled:opacity-50"
          :disabled="pending"
          @click="onAnswer('REMEMBERED')"
        >
          {{ pending ? "Saving..." : "✅ 思い出せた" }}
        </button>

        <button class="px-4 py-2 border rounded disabled:opacity-50" :disabled="pending" @click="onAnswer('UNCERTAIN')">
          {{ pending ? "Saving..." : "❓ 怪しい" }}
        </button>
      </div>

      <div class="text-sm opacity-60">
        {{ Math.min(index + 1, due.length === 0 ? 0 : index + 1) }} / {{ due.length + 1 }}
      </div>
    </section>
  </main>
</template>
