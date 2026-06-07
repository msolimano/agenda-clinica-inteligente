import { Activity } from 'lucide-react';

function App() {
  return (
    <main className="app-shell">
      <section className="app-shell__brand" aria-label="I-Clinical Technology">
        <div className="app-shell__mark">
          <Activity aria-hidden="true" size={32} strokeWidth={2.25} />
        </div>
        <div className="app-shell__copy">
          <p className="app-shell__eyebrow">Information Clinical Technology</p>
          <h1 className="app-shell__title">I-Clinical Technology</h1>
          <p className="app-shell__slogan">Conectando informacion clinica con inteligencia</p>
        </div>
      </section>
    </main>
  );
}

export default App;
