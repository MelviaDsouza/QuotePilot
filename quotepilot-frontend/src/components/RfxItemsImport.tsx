import { useState } from "react";

type Props = {
  rfxId: string;
  onImported?: () => void;
};

export default function RfxItemsImport({ rfxId, onImported }: Props) {
  const [file, setFile] = useState<File | null>(null);
  const [busy, setBusy] = useState(false);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  const handleImport = async () => {
    if (!file) {
      setError("Choose an Excel file first.");
      return;
    }

    setBusy(true);
    setError("");
    setMessage("");

    try {
      const formData = new FormData();
      formData.append("file", file);

      const response = await fetch(
        `http://localhost:8080/api/rfx/${encodeURIComponent(rfxId)}/items/import`,
        {
          method: "POST",
          body: formData,
        }
      );

      if (!response.ok) {
        const body = await response.text();
        throw new Error(body || `Import failed (${response.status})`);
      }

      const result = await response.json().catch(() => null);
      setMessage(
        result?.message ??
          (result?.importedCount != null
            ? `${result.importedCount} items imported successfully.`
            : "Items imported successfully.")
      );
      setFile(null);
      onImported?.();
    } catch (e) {
      setError(e instanceof Error ? e.message : "Unable to import items.");
    } finally {
      setBusy(false);
    }
  };

  return (
    <section className="rounded-2xl border border-slate-200 bg-white p-6">
      <div className="mb-4">
        <h3 className="text-lg font-semibold">Import RFx line items</h3>
        <p className="mt-1 text-sm text-slate-500">
          Upload the Excel workbook containing the <code>RFx_Items</code> sheet.
        </p>
      </div>

      <div className="flex flex-col gap-3 sm:flex-row sm:items-center">
        <input
          type="file"
          accept=".xlsx,.xls"
          onChange={(e) => setFile(e.target.files?.[0] ?? null)}
          className="block w-full text-sm text-slate-600 file:mr-4 file:rounded-lg file:border-0 file:bg-indigo-50 file:px-4 file:py-2 file:font-medium file:text-indigo-700 hover:file:bg-indigo-100"
        />
        <button
          type="button"
          onClick={handleImport}
          disabled={!file || busy || !rfxId}
          className="shrink-0 rounded-lg bg-indigo-600 px-4 py-2.5 text-sm font-medium text-white hover:bg-indigo-700 disabled:cursor-not-allowed disabled:bg-slate-300"
        >
          {busy ? "Importing…" : "Import items"}
        </button>
      </div>

      {file && <p className="mt-2 text-xs text-slate-500">Selected: {file.name}</p>}
      {message && <p className="mt-3 text-sm text-green-700">{message}</p>}
      {error && <p className="mt-3 text-sm text-red-600">{error}</p>}
    </section>
  );
}
