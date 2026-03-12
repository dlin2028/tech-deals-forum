<script>
  import { createEventDispatcher } from 'svelte';

  export let tags = [];
  export let placeholder = 'Add a tag...';

  const dispatch = createEventDispatcher();
  let inputValue = '';

  function addTag() {
    const tag = inputValue.trim();
    if (tag && !tags.includes(tag)) {
      tags = [...tags, tag];
      dispatch('change', tags);
    }
    inputValue = '';
  }

  function removeTag(tag) {
    tags = tags.filter(t => t !== tag);
    dispatch('change', tags);
  }

  function handleKeydown(e) {
    if (e.key === 'Enter' || e.key === ',') {
      e.preventDefault();
      addTag();
    } else if (e.key === 'Backspace' && !inputValue && tags.length > 0) {
      removeTag(tags[tags.length - 1]);
    }
  }
</script>

<div class="border border-gray-300 rounded-lg p-2 flex flex-wrap gap-2 focus-within:ring-2 focus-within:ring-blue-500 focus-within:border-transparent bg-white min-h-[44px]">
  {#each tags as tag}
    <span class="inline-flex items-center gap-1 bg-blue-100 text-blue-800 text-sm px-2.5 py-0.5 rounded-full">
      {tag}
      <button type="button" on:click={() => removeTag(tag)} class="text-blue-600 hover:text-blue-800 font-bold leading-none">×</button>
    </span>
  {/each}
  <input
    type="text"
    bind:value={inputValue}
    on:keydown={handleKeydown}
    on:blur={addTag}
    placeholder={tags.length === 0 ? placeholder : ''}
    class="flex-1 min-w-[120px] outline-none text-sm bg-transparent"
  />
</div>
