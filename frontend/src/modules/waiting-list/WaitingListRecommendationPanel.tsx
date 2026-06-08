import { Lightbulb, Search } from 'lucide-react';
import { useEffect, useState } from 'react';
import { addMinutes, localDateTimeToIso, toDateTimeLocalValue } from './waitingListDate';
import type { ProfessionalOption, SpecialtyOption, WaitingListEntry, WaitingListRecommendation } from './waitingList.types';

interface WaitingListRecommendationPanelProps {
  professionals: ProfessionalOption[];
  specialties: SpecialtyOption[];
  recommendations: WaitingListRecommendation[];
  defaultEntry?: WaitingListEntry;
  onRecommend: (params: { professionalId?: string; specialtyId?: string; startAt: string; endAt: string }) => Promise<void>;
  onSchedule: (waitingListId: string, payload: { professionalId?: string; startAt: string; endAt: string; appointmentType: 'consultation'; reason: string }) => Promise<void>;
}

export function WaitingListRecommendationPanel({ professionals, specialties, recommendations, defaultEntry, onRecommend, onSchedule }: WaitingListRecommendationPanelProps) {
  const initialStart = toDateTimeLocalValue(new Date());
  const [professionalId, setProfessionalId] = useState('');
  const [specialtyId, setSpecialtyId] = useState('');
  const [startAt, setStartAt] = useState(initialStart);
  const [endAt, setEndAt] = useState(addMinutes(initialStart, 30));
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    if (!defaultEntry) return;
    setProfessionalId(defaultEntry.professionalId ?? '');
    setSpecialtyId(defaultEntry.specialtyId);
  }, [defaultEntry]);

  async function handleRecommend() {
    setIsLoading(true);
    try {
      await onRecommend({
        professionalId: professionalId || undefined,
        specialtyId: specialtyId || undefined,
        startAt: localDateTimeToIso(startAt),
        endAt: localDateTimeToIso(endAt)
      });
    } finally {
      setIsLoading(false);
    }
  }

  async function handleSchedule(recommendation: WaitingListRecommendation) {
    await onSchedule(recommendation.waitingListId, {
      professionalId: professionalId || recommendation.professionalId || undefined,
      startAt: localDateTimeToIso(startAt),
      endAt: localDateTimeToIso(endAt),
      appointmentType: 'consultation',
      reason: `Agendada desde lista de espera: ${recommendation.reason}`
    });
  }

  return (
    <section className="waiting-recommendations" aria-label="Recomendaciones de lista de espera">
      <div className="waiting-recommendations__header">
        <span className="waiting-recommendations__icon"><Lightbulb aria-hidden="true" size={20} /></span>
        <div>
          <h2 className="waiting-recommendations__title">Cupo liberado</h2>
          <p className="waiting-recommendations__subtitle">Recomendación determinística, sin IA generativa.</p>
        </div>
      </div>
      <div className="waiting-recommendations__grid">
        <label>
          <span>Profesional</span>
          <select value={professionalId} onChange={(event) => setProfessionalId(event.target.value)}>
            <option value="">Sin filtro</option>
            {professionals.map((professional) => <option key={professional.id} value={professional.id}>{professional.firstName} {professional.lastName}</option>)}
          </select>
        </label>
        <label>
          <span>Especialidad</span>
          <select value={specialtyId} onChange={(event) => setSpecialtyId(event.target.value)}>
            <option value="">Sin filtro</option>
            {specialties.map((specialty) => <option key={specialty.id} value={specialty.id}>{specialty.name}</option>)}
          </select>
        </label>
        <label>
          <span>Inicio</span>
          <input type="datetime-local" value={startAt} onChange={(event) => setStartAt(event.target.value)} />
        </label>
        <label>
          <span>Fin</span>
          <input type="datetime-local" value={endAt} onChange={(event) => setEndAt(event.target.value)} />
        </label>
      </div>
      <button className="waiting-recommendations__button" type="button" onClick={() => void handleRecommend()} disabled={isLoading || (!professionalId && !specialtyId)}>
        <Search aria-hidden="true" size={17} />
        {isLoading ? 'Buscando' : 'Recomendar'}
      </button>
      <div className="waiting-recommendations__list">
        {recommendations.map((recommendation) => (
          <article className="waiting-recommendations__item" key={recommendation.waitingListId}>
            <div>
              <strong>{recommendation.patientName}</strong>
              <span>{recommendation.specialtyName} · prioridad {recommendation.priority} · score {recommendation.score}</span>
              <p>{recommendation.reason}</p>
            </div>
            <button type="button" onClick={() => void handleSchedule(recommendation)}>Agendar</button>
          </article>
        ))}
        {recommendations.length === 0 ? <div className="waiting-recommendations__empty">Sin recomendaciones cargadas.</div> : null}
      </div>
    </section>
  );
}
