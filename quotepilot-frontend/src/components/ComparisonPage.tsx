
import { useEffect, useState } from "react";
import { getComparison } from "../services/api";

type Item = {
  itemId: string;
  description?: string;
};

type Quote = {
  quoteId: string;
  vendorId: string;
  vendorName: string;
  itemId: string;
  quotedPrice: number;
  currency: string;
  quotedUnit: string;
  normalizedPriceInr: number | null;
  eligible: boolean;
  exclusionReason: string | null;
  winner: boolean;
  freightIncluded: boolean;
  freightAmount: number | null;
  freightType: string;
};

type ComparisonResult = {
  itemId: string;
  quotes: Quote[];
};

type Props = {
  rfxId: string;
  items: Item[];
};

export default function ComparisonPage({ rfxId, items }: Props) {
  const [results, setResults] = useState<ComparisonResult[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    if (!rfxId || items.length === 0) return;

    setLoading(true);
    setError("");

    getComparison(
      rfxId,
      items.map((item) => item.itemId)
    )
      .then((response) => setResults(response.data))
      .catch(() => setError("Unable to load comparison. Please check saved quotes."))
      .finally(() => setLoading(false));
  }, [rfxId, items]);

  if (loading) {
    return <div className="p-8">Loading comparison...</div>;
  }

  if (error) {
    return <div className="p-8 text-red-600">{error}</div>;
  }

  return (
    <div className="p-8 max-w-[1600px] mx-auto">
      <h1 className="text-2xl font-semibold text-slate-800 mb-2">
        Supplier Comparison
      </h1>

      <p className="text-slate-500 mb-6">
        Compare supplier prices, eligibility, and award winners across RFx items.
      </p>

      {results.map((item) => (
        <div
          key={item.itemId}
          className="bg-white border border-slate-200 rounded-xl mb-6 overflow-hidden"
        >
          <div className="px-5 py-4 bg-slate-50 border-b">
            <h2 className="font-semibold text-slate-800">
              {item.itemId}{" "}
              <span className="font-normal text-slate-500">
                {items.find((i) => i.itemId === item.itemId)?.description}
              </span>
            </h2>
          </div>

          <div className="overflow-x-auto">
            <table className="w-full text-sm text-left">
              <thead className="bg-white text-slate-500">
                <tr>
                  <th className="px-5 py-3">Supplier</th>
                  <th className="px-5 py-3">Quoted Price</th>
                  <th className="px-5 py-3">Normalized INR</th>
                  <th className="px-5 py-3">Freight</th>
                  <th className="px-5 py-3">Eligibility</th>
                  <th className="px-5 py-3">Result</th>
                </tr>
              </thead>

              <tbody>
                {item.quotes.map((quote) => (
                  <tr key={quote.quoteId} className="border-t">
                    <td className="px-5 py-3 font-medium text-slate-800">
                      {quote.vendorName}
                    </td>

                    <td className="px-5 py-3">
                      {quote.currency} {quote.quotedPrice} {quote.quotedUnit}
                    </td>

                    <td className="px-5 py-3">
                      {quote.normalizedPriceInr != null
                        ? `₹${quote.normalizedPriceInr.toFixed(2)}`
                        : "—"}
                    </td>

                    <td className="px-5 py-3">
                      {quote.freightIncluded
                        ? "Included"
                        : quote.freightAmount != null
                        ? `₹${quote.freightAmount} (${quote.freightType})`
                        : "Not included"}
                    </td>

                    <td className="px-5 py-3">
                      {quote.eligible ? (
                        <span className="text-green-700 font-medium">Eligible</span>
                      ) : (
                        <span className="text-red-600" title={quote.exclusionReason ?? ""}>
                          Ineligible
                        </span>
                      )}
                    </td>

                    <td className="px-5 py-3">
                      {quote.winner && (
                        <span className="text-indigo-700 font-semibold">
                          Winner
                        </span>
                      )}
                      {!quote.winner && !quote.eligible && (
                        <span className="text-slate-400">Excluded</span>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      ))}

      {results.length === 0 && (
        <p className="text-slate-500">No comparison results available.</p>
      )}
    </div>
  );
}
