type ApiError = {
  statusCode?: number;
  message?: string;
  // Micronautのエラーフォーマットは色々あるので「とりあえず」受ける
  _embedded?: { errors?: Array<{ message?: string }> };
};

function extractMessage(err: unknown): string {
  // Nuxt $fetch は FetchError を投げることがある
  const anyErr = err as any;
  const data = (anyErr?.data ?? anyErr?.response?._data) as ApiError | undefined;

  const embeddedMsg = data?._embedded?.errors?.[0]?.message;
  if (embeddedMsg) return embeddedMsg;

  if (typeof data?.message === "string") return data.message;
  if (typeof anyErr?.message === "string") return anyErr.message;

  return "Request failed";
}

export function useApi() {
  const config = useRuntimeConfig();
  const baseURL = config.public.apiBaseUrl as string;

  const client = $fetch.create({ baseURL });

  return {
    async get<TResponse>(path: string): Promise<TResponse> {
      try {
        return await client<TResponse>(path, { method: "GET" });
      } catch (e) {
        throw new Error(extractMessage(e));
      }
    },

    async post<TRequest extends Record<string, any> | BodyInit | null | undefined, TResponse>(
      path: string,
      body: TRequest
    ): Promise<TResponse> {
      try {
        return await client<TResponse>(path, {
          method: "POST",
          body,
        });
      } catch (e) {
        throw new Error(extractMessage(e));
      }
    },
  };
}
