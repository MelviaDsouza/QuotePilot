
import { useEffect, useState } from "react";
import { getAllRfxs, createRfx } from "../services/api";

interface Rfx {
  rfxId: string;
  title: string;
  description: string;
  status: string;
  submissionDeadline: string;
}

interface Props {
  onSelect: (rfxId: string) => void;
  selectedRfxId: string;
}

export default function RfxManager({
  onSelect,
  selectedRfxId,
}: Props) {
  const [rfxs, setRfxs] = useState<Rfx[]>([]);
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [deadline, setDeadline] = useState("");
  const [creating, setCreating] = useState(false);
  const [error, setError] = useState("");

  const loadRfxs = async () => {
    try {
      const response = await getAllRfxs();
      setRfxs(response.data);
    } catch {
      setError("Unable to load RFxs.");
    }
  };

  useEffect(() => {
    loadRfxs();
  }, []);

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    setCreating(true);
    setError("");

    try {
      const response = await createRfx({
        title,
        description,
        submissionDeadline: deadline,
        createdBy: "Melvia",
      });

      setTitle("");
      setDescription("");
      setDeadline("");

      await loadRfxs();
      onSelect(response.data.rfxId);
    } catch {
      setError("Unable to create RFx.");
    } finally {
      setCreating(false);
    }
  };

  return (
    <div className="space-y-6">
      <section className="bg-white border rounded-xl p-6">
        <h2 className="text-lg font-semibold mb-4">
          Create a new RFx
        </h2>

        <form onSubmit={handleCreate} className="space-y-3">
          <input
            required
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            placeholder="RFx title"
            className="w-full border rounded-lg p-3"
          />

          <textarea
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            placeholder="Description"
            className="w-full border rounded-lg p-3"
          />

          <input
            required
            type="date"
            value={deadline}
            onChange={(e) => setDeadline(e.target.value)}
            className="border rounded-lg p-3"
          />

          <button
            disabled={creating}
            className="bg-indigo-600 text-white px-5 py-2 rounded-lg disabled:opacity-50"
          >
            {creating ? "Creating..." : "Create RFx"}
          </button>
        </form>

        {error && (
          <p className="text-red-600 text-sm mt-3">{error}</p>
        )}
      </section>

      <section className="bg-white border rounded-xl p-6">
        <h2 className="text-lg font-semibold mb-4">
          Existing RFxs
        </h2>

        <div className="space-y-3">
          {rfxs.map((rfx) => (
            <button
              key={rfx.rfxId}
              onClick={() => onSelect(rfx.rfxId)}
              className={`w-full text-left border rounded-lg p-4 ${
                selectedRfxId === rfx.rfxId
                  ? "border-indigo-500 bg-indigo-50"
                  : "hover:bg-slate-50"
              }`}
            >
              <div className="font-medium">{rfx.title}</div>
              <div className="text-sm text-slate-500">
                {rfx.rfxId} · {rfx.status}
              </div>
              <div className="text-xs text-slate-400 mt-1">
                Deadline: {rfx.submissionDeadline}
              </div>
            </button>
          ))}
        </div>
      </section>
    </div>
  );
}
