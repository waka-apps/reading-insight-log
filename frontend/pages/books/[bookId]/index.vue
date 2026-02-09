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
</script>

<template>
  <main class="p-6 max-w-3xl mx-auto">
    <header class="mb-6">
      <NuxtLink to="/books" class="underline opacity-80">← Back</NuxtLink>
      <h1 class="text-2xl font-bold mt-3">Book Detail</h1>
      <p class="opacity-80 mt-1">bookId: {{ bookId }}</p>
    </header>

    <section class="border rounded p-4">
      <p class="opacity-70">This page will show insights for the book.</p>
      <div class="mt-4">
        <NuxtLink :to="`/books/${bookId}/new-insight`" class="px-3 py-2 rounded border inline-block">
          + New Insight
        </NuxtLink>
      </div>
    </section>
  </main>
</template>
