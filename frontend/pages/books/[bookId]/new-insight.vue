<script setup lang="ts">
import type { CreateInsightRequest, Insight } from "~/types/insight";

const api = useApi();
const route = useRoute();

const bookId = computed(() => String(route.params.bookId));
const quote = ref("");
const interpretation = ref("");
const tagsInput = ref("");

const pending = ref(false);
const error = ref<string | null>(null);

const canSave = computed(
  () => quote.value.trim().length > 0 && interpretation.value.trim().length > 0 && !pending.value,
);

function parseTags(input: string): string[] {
  return input
    .split(",")
    .map((t) => t.trim())
    .filter(Boolean);
}

async function onSave() {
  error.value = null;
  pending.value = true;
  try {
    const body: CreateInsightRequest = {
      quote: quote.value,
      interpretation: interpretation.value,
      tags: parseTags(tagsInput.value),
    };
    await api.post<CreateInsightRequest, Insight>(`/admin/books/${bookId.value}/insights`, body);
    await navigateTo(`/books/${bookId.value}`);
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
      <NuxtLink :to="`/books/${bookId}`" class="underline opacity-80">← Back</NuxtLink>
      <h1 class="text-2xl font-bold mt-3">New Insight</h1>
      <p class="opacity-80 mt-1">for bookId: {{ bookId }}</p>
    </header>

    <section v-if="error" class="border rounded p-4 mb-4">
      <p class="font-semibold">Error</p>
      <p class="opacity-80 mt-1">{{ error }}</p>
    </section>

    <form class="space-y-4" @submit.prevent="onSave">
      <div>
        <label class="block font-medium mb-1">Quote (required)</label>
        <textarea v-model="quote" rows="4" class="w-full border rounded p-2" :disabled="pending" />
      </div>

      <div>
        <label class="block font-medium mb-1">Interpretation (required)</label>
        <textarea v-model="interpretation" rows="4" class="w-full border rounded p-2" :disabled="pending" />
      </div>

      <div>
        <label class="block font-medium mb-1">Tags (comma-separated)</label>
        <input
          v-model="tagsInput"
          class="w-full border rounded p-2"
          placeholder="kotlin, jvm, entrypoint"
          :disabled="pending"
        />
      </div>

      <button type="submit" class="px-4 py-2 rounded border" :disabled="!canSave">
        {{ pending ? "Saving..." : "Save" }}
      </button>
    </form>
  </main>
</template>
