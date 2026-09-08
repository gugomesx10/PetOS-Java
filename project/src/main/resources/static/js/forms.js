// Melhoria progressiva: formulários e segurança funcionam sem JavaScript.
document.addEventListener('submit', (event) => {
  const form = event.target;
  if (!(form instanceof HTMLFormElement) || form.method.toLowerCase() !== 'post') return;
  if (form.dataset.submitting) { event.preventDefault(); return; }
  form.dataset.submitting = 'true';
  form.setAttribute('aria-busy', 'true');
  const button = event.submitter;
  if (button) {
    button.dataset.originalText = button.textContent;
    button.textContent = 'Processando cuidado…';
    button.disabled = true;
  }
});
window.addEventListener('pageshow', () => {
  document.querySelectorAll('form[data-submitting]').forEach((form) => {
    delete form.dataset.submitting;
    form.removeAttribute('aria-busy');
    form.querySelectorAll('[data-original-text]').forEach((button) => {
      button.textContent = button.dataset.originalText;
      button.disabled = false;
      delete button.dataset.originalText;
    });
  });
});

