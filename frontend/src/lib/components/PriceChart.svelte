<script>
  import { onMount, onDestroy } from 'svelte';

  export let priceHistory = [];

  let canvas;
  let chart;

  onMount(async () => {
    if (!priceHistory || priceHistory.length === 0) return;
    const { Chart, registerables } = await import('chart.js');
    Chart.register(...registerables);

    const labels = priceHistory.map(p => {
      const d = new Date(p.recorded_at || p.date);
      return d.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
    });
    const prices = priceHistory.map(p => p.price);

    chart = new Chart(canvas, {
      type: 'line',
      data: {
        labels,
        datasets: [{
          label: 'Price ($)',
          data: prices,
          borderColor: '#2563eb',
          backgroundColor: 'rgba(37, 99, 235, 0.1)',
          borderWidth: 2,
          fill: true,
          tension: 0.4,
          pointBackgroundColor: '#2563eb',
          pointRadius: 4,
          pointHoverRadius: 6
        }]
      },
      options: {
        responsive: true,
        plugins: {
          legend: { display: false },
          tooltip: {
            callbacks: {
              label: ctx => `$${ctx.raw}`
            }
          }
        },
        scales: {
          y: {
            beginAtZero: false,
            ticks: {
              callback: val => `$${val}`
            },
            grid: { color: 'rgba(0,0,0,0.05)' }
          },
          x: {
            grid: { display: false }
          }
        }
      }
    });
  });

  onDestroy(() => {
    if (chart) chart.destroy();
  });
</script>

{#if priceHistory && priceHistory.length > 0}
  <canvas bind:this={canvas}></canvas>
{:else}
  <div class="text-center py-8 text-gray-500">
    <svg class="w-12 h-12 mx-auto mb-2 text-gray-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"/>
    </svg>
    <p>No price history available yet</p>
  </div>
{/if}
