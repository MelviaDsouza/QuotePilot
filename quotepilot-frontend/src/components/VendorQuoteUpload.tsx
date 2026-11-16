
import { useState } from "react";
import { Upload } from "lucide-react";

type RfxItem = {
    itemId: string;
    description: string;
};

type ExtractedQuoteLine = {
    itemId: string;
    description: string;
    quotedPrice: number | null;
    currency: string;
    quotedUnit: string;
    quantity: number | null;
    freightTerms: string;
    freightAmount: number | null;
    confidence: number | null;
    evidence: string;
    evidenceVerified: boolean | null;
    evidenceStatus: string;
    validationStatus: string;
    validationErrors: string[];
    validationWarnings: string[];
};

type ReviewResponse = {
    extraction: {
        documentName: string;
        vendorName: string;
        currency: string;
        freightTerms: string;
        validity: string;
        lines: ExtractedQuoteLine[];
        warnings: string[];
        extractionStatus: string;
    };
    vendorMatch: {
        vendorId: string;
        suggestedVendorName: string;
        status: string;
        message: string;
    };
};

type Props = {
    rfxId: string;
    items: RfxItem[];
    onQuotesSaved?: () => void;
};

export default function VendorQuoteUpload({
    rfxId,
    items,
    onQuotesSaved,
}: Props) {
    const [file, setFile] = useState<File | null>(null);
    const [review, setReview] = useState<ReviewResponse | null>(null);
    const [lines, setLines] = useState<ExtractedQuoteLine[]>([]);
    const [vendorId, setVendorId] = useState("");
    const [loading, setLoading] = useState(false);
    const [saving, setSaving] = useState(false);
    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");

    const API = "http://localhost:8080";

    async function reviewDocument() {
        if (!file) {
            setError("Please select a supplier document.");
            return;
        }

        setLoading(true);
        setError("");
        setSuccess("");
        setReview(null);
        setLines([]);

        try {
            const formData = new FormData();
            formData.append("file", file);

            const response = await fetch(
                `${API}/api/rfx/${rfxId}/documents/review`,
                {
                    method: "POST",
                    body: formData,
                }
            );

            if (!response.ok) {
                throw new Error(await response.text());
            }

            const result: ReviewResponse = await response.json();

            setReview(result);
            setLines(result.extraction.lines ?? []);
            setVendorId(result.vendorMatch.vendorId ?? "");
        } catch (e) {
            setError(
                e instanceof Error ? e.message : "Document review failed."
            );
        } finally {
            setLoading(false);
        }
    }

    function updateLine(
        index: number,
        field: keyof ExtractedQuoteLine,
        value: string | number | null
    ) {
        setLines((previous) =>
            previous.map((line, i) =>
                i === index ? { ...line, [field]: value } : line
            )
        );
    }

    async function confirmQuotes() {
        if (!review) return;

        if (!vendorId.trim()) {
            setError("Please provide a vendor ID.");
            return;
        }

        if (lines.length === 0) {
            setError("There are no quote lines to save.");
            return;
        }

        const invalid = lines.some(
            (line) =>
                !line.itemId ||
                line.quotedPrice == null ||
                line.quotedPrice <= 0 ||
                !line.currency ||
                !line.quotedUnit
        );

        if (invalid) {
            setError(
                "Every line needs an item, positive price, currency, and quoted unit."
            );
            return;
        }

        setSaving(true);
        setError("");
        setSuccess("");

        try {
            const payload = {
                vendorId,
                documentName: review.extraction.documentName,
                lines: lines.map((line) => ({
                    itemId: line.itemId,
                    description: line.description,
                    quotedPrice: line.quotedPrice,
                    currency: line.currency,
                    quotedUnit: line.quotedUnit,
                    quantity: line.quantity,
                    freightTerms: line.freightTerms,
                    freightAmount: Number(line.freightAmount || 0),
                    evidence: line.evidence,
                })),
            };

            const response = await fetch(
                `${API}/api/rfx/${rfxId}/documents/confirm`,
                {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json",
                    },
                    body: JSON.stringify(payload),
                }
            );

            if (!response.ok) {
                throw new Error(await response.text());
            }

            setSuccess("Supplier quotes saved successfully.");
            setReview(null);
            setLines([]);
            setFile(null);
            onQuotesSaved?.();
        } catch (e) {
            setError(
                e instanceof Error ? e.message : "Could not save quotes."
            );
        } finally {
            setSaving(false);
        }
    }

    return (
        <div className="space-y-6">

            {/* Upload */}
            <div className="rounded-xl border p-5 space-y-4">
                <h3 className="font-semibold">Upload supplier document</h3>

                <label className="inline-flex items-center gap-2 rounded-lg bg-indigo-600 px-4 py-2.5 text-sm font-medium text-white cursor-pointer hover:bg-indigo-700">
                    <Upload size={10} />
                    Choose Supplier Document

                    <input
                        type="file"
                        accept=".pdf,.xls,.xlsx,.doc,.docx,.eml,.txt,.png,.jpg,.jpeg"
                        onChange={(e) => {
                            setFile(e.target.files?.[0] ?? null);
                            setReview(null);
                            setLines([]);
                            setError("");
                            setSuccess("");
                        }}
                        className="hidden"
                    />
                </label>

                {file && (
                    <p className="mt-2 text-sm text-slate-600">
                        Selected file: {file.name}
                    </p>
                )}

                <button
                    onClick={reviewDocument}
                    disabled={!file || loading}
                    className="ml-4 rounded-lg bg-blue-600 px-4 py-2 text-white disabled:opacity-50"
                >
                    {loading ? "Extracting..." : "Review Document"}
                </button>
            </div>

            {error && (
                <div className="rounded-lg bg-red-50 p-3 text-red-700">
                    {error}
                </div>
            )}

            {success && (
                <div className="rounded-lg bg-green-50 p-3 text-green-700">
                    {success}
                </div>
            )}

            {/* Review */}
            {review && (
                <div className="space-y-5">
                    <div className="rounded-xl border p-5 space-y-3">
                        <h3 className="font-semibold">Extraction Summary</h3>

                        <p>
                            <strong>Supplier detected:</strong>{" "}
                            {review.extraction.vendorName || "Unknown"}
                        </p>

                        <p>
                            <strong>Suggested supplier:</strong>{" "}
                            {review.vendorMatch.suggestedVendorName || "No match"}
                        </p>

                        <p>
                            <strong>Match status:</strong>{" "}
                            {review.vendorMatch.status}
                        </p>

                        <p>
                            <strong>Match message:</strong>{" "}
                            {review.vendorMatch.message}
                        </p>

                        <label className="block text-sm font-medium">
                            Vendor ID
                            <input
                                value={vendorId}
                                onChange={(e) => setVendorId(e.target.value)}
                                className="mt-1 block w-full rounded border p-2"
                                placeholder="Enter registered vendor ID"
                            />
                        </label>

                        <p>
                            <strong>Currency:</strong>{" "}
                            {review.extraction.currency}
                        </p>

                        <p>
                            <strong>Freight terms:</strong>{" "}
                            {review.extraction.freightTerms}
                        </p>

                        <p>
                            <strong>Validity:</strong>{" "}
                            {review.extraction.validity}
                        </p>

                        <p>
                            <strong>Extraction status:</strong>{" "}
                            {review.extraction.extractionStatus}
                        </p>

                        {review.extraction.warnings?.length > 0 && (
                            <div className="rounded bg-yellow-50 p-3 text-yellow-800">
                                <strong>Document warnings</strong>
                                <ul className="list-disc pl-5">
                                    {review.extraction.warnings.map((warning, i) => (
                                        <li key={i}>{warning}</li>
                                    ))}
                                </ul>
                            </div>
                        )}
                    </div>

                    {/* Quote lines */}
                    <div className="rounded-xl border p-5 space-y-4">
                        <h3 className="font-semibold">
                            Review extracted quote lines
                        </h3>

                        <div className="overflow-x-auto">
                            <table className="min-w-full text-sm">
                                <thead>
                                    <tr className="border-b text-left">
                                        <th className="p-2">RFx Item</th>
                                        <th className="p-2">Description</th>
                                        <th className="p-2">Price</th>
                                        <th className="p-2">Currency</th>
                                        <th className="p-2">Unit</th>
                                        <th className="p-2">Qty</th>
                                        <th className="p-2">Freight</th>
                                        <th className="p-2">Evidence</th>
                                        <th className="p-2">Validation</th>
                                    </tr>
                                </thead>

                                <tbody>
                                    {lines.map((line, index) => (
                                        <tr key={index} className="border-b align-top">
                                            <td className="p-2">
                                                <select
                                                    value={line.itemId ?? ""}
                                                    onChange={(e) =>
                                                        updateLine(index, "itemId", e.target.value)
                                                    }
                                                    className="min-w-28 rounded border p-1"
                                                >
                                                    <option value="">Select item</option>
                                                    {items.map((item) => (
                                                        <option key={item.itemId} value={item.itemId}>
                                                            {item.itemId}
                                                        </option>
                                                    ))}
                                                </select>
                                            </td>

                                            <td className="p-2">
                                                <input
                                                    value={line.description ?? ""}
                                                    onChange={(e) =>
                                                        updateLine(index, "description", e.target.value)
                                                    }
                                                    className="min-w-40 rounded border p-1"
                                                />
                                            </td>

                                            <td className="p-2">
                                                <input
                                                    type="number"
                                                    min="0"
                                                    step="any"
                                                    value={line.quotedPrice ?? ""}
                                                    onChange={(e) =>
                                                        updateLine(
                                                            index,
                                                            "quotedPrice",
                                                            e.target.value === ""
                                                                ? null
                                                                : Number(e.target.value)
                                                        )
                                                    }
                                                    className="w-24 rounded border p-1"
                                                />
                                            </td>

                                            <td className="p-2">
                                                <input
                                                    value={line.currency ?? ""}
                                                    onChange={(e) =>
                                                        updateLine(index, "currency", e.target.value)
                                                    }
                                                    className="w-20 rounded border p-1"
                                                />
                                            </td>

                                            <td className="p-2">
                                                <input
                                                    value={line.quotedUnit ?? ""}
                                                    onChange={(e) =>
                                                        updateLine(index, "quotedUnit", e.target.value)
                                                    }
                                                    className="w-24 rounded border p-1"
                                                />
                                            </td>

                                            <td className="p-2">
                                                <input
                                                    type="number"
                                                    min="0"
                                                    value={line.quantity ?? ""}
                                                    onChange={(e) =>
                                                        updateLine(
                                                            index,
                                                            "quantity",
                                                            e.target.value === ""
                                                                ? null
                                                                : Number(e.target.value)
                                                        )
                                                    }
                                                    className="w-20 rounded border p-1"
                                                />
                                            </td>

                                            <td className="p-2">
                                                <input
                                                    value={line.freightTerms ?? ""}
                                                    onChange={(e) =>
                                                        updateLine(index, "freightTerms", e.target.value)
                                                    }
                                                    className="w-28 rounded border p-1"
                                                />
                                            </td>
                                            <input
                                                type="number"
                                                min="0"
                                                value={line.freightAmount ?? ""}
                                                onChange={(e) =>
                                                    updateLine(
                                                        index,
                                                        "freightAmount",
                                                        e.target.value === "" ? null : Number(e.target.value)
                                                    )
                                                }
                                                className="w-24 rounded border p-1"
                                                placeholder="Amount"
                                            />

                                            <td className="p-2">
                                                <textarea
                                                    value={line.evidence ?? ""}
                                                    onChange={(e) =>
                                                        updateLine(index, "evidence", e.target.value)
                                                    }
                                                    className="min-w-48 rounded border p-1"
                                                    rows={3}
                                                />
                                                <div className="mt-1 text-xs text-gray-500">
                                                    {line.evidenceStatus}
                                                </div>
                                            </td>

                                            <td className="p-2">
                                                <div>{line.validationStatus}</div>
                                                {line.validationErrors?.map((message, i) => (
                                                    <div key={i} className="text-red-600">
                                                        {message}
                                                    </div>
                                                ))}
                                                {line.validationWarnings?.map((message, i) => (
                                                    <div key={i} className="text-yellow-700">
                                                        {message}
                                                    </div>
                                                ))}
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>

                        <button
                            onClick={() =>
                                setLines((previous) => [
                                    ...previous,
                                    {
                                        itemId: "",
                                        description: "",
                                        quotedPrice: null,
                                        currency: "INR",
                                        quotedUnit: "per box",
                                        quantity: null,
                                        freightTerms: "INCLUDED",
                                        freightAmount:null,
                                        confidence: null,
                                        evidence: "",
                                        evidenceVerified: false,
                                        evidenceStatus: "PENDING",
                                        validationStatus: "PENDING",
                                        validationErrors: [],
                                        validationWarnings: [],
                                    },
                                ])
                            }
                            className="rounded border px-3 py-2"
                        >
                            + Add Quote Line
                        </button>

                        <button
                            onClick={confirmQuotes}
                            disabled={saving || lines.length === 0}
                            className="ml-3 rounded-lg bg-green-600 px-4 py-2 text-white disabled:opacity-50"
                        >
                            {saving ? "Saving..." : "Confirm & Save Quotes"}
                        </button>
                    </div>
                </div>
            )}
        </div>
    );
}
