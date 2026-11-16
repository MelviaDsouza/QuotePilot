
import { useEffect, useState } from "react";
import AnalystChat from "./components/AnalystChat";
import VendorQuoteUpload from "./components/VendorQuoteUpload";
import ComparisonPage from "./components/ComparisonPage";
import {
  LayoutDashboard, FileText, Upload, GitCompare, Trophy,
  Settings, Search, Bell, ChevronDown, Package, Truck,
  ShieldCheck, Sparkles, CheckCircle2, AlertTriangle,
  RefreshCw, Mail, X, Send,
} from "lucide-react";
import RfxItemsImport from "./components/RfxItemsImport";

import {
  getItems,
  getQuotes,
  getAwardRecommendation,
  getAwardReadiness,
  approveAward,
  getAwardApproval,
} from "./services/api";


type AwardLine = {
  itemId: string;
  quantity: number;
  quoteId: string;
  totalPriceInr: number;
  unitPriceInr: number;
  vendorId: string;
  vendorName: string;
};

type VendorSummary = {
  itemsAwarded: number;
  totalAwardValueInr: number;
  vendorId: string;
  vendorName: string;
};

type Award = {
  additionalFreightInr: number;
  awardLines: AwardLine[];
  awardedItems: number;
  quotedAwardValueInr: number;
  totalAwardValueInr: number;
  totalItems: number;
  totalLandedCostInr: number;
  unawardedItemIds: string[];
  unawardedItems: number;
  vendorSummaries: VendorSummary[];
};

type Readiness = {
  issues: string[];
  passedChecks: number;
  passedChecksList: string[];
  readyForApproval: boolean;
  totalChecks: number;
  unresolvedIssues: number;
};
type Approval = {
  status: string;
  approvedAt: string;
  approvedAwardValueInr: number;
  awardedItems: number;
};

const money = (value = 0) =>
  new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency: "INR",
    maximumFractionDigits: 0,
  }).format(value);

const compactMoney = (value = 0) =>
  `₹${(value / 100000).toFixed(2)}L`;

function App() {
  const [selectedRfxId, setSelectedRfxId] = useState("RFX-001");
  const [activePage, setActivePage] = useState("Overview");
  const [rfxList, setRfxList] = useState<{ rfxId: string; title: string; status?: string }[]>([]);
  const [showCreateRfx, setShowCreateRfx] = useState(false);
  const [newRfxTitle, setNewRfxTitle] = useState("");
  const [newRfxDescription, setNewRfxDescription] = useState("");
  const [newRfxDeadline, setNewRfxDeadline] = useState("");
  const [rfxError, setRfxError] = useState("");
  const [aiPrompt, setAiPrompt] = useState("");
  const [aiDraft, setAiDraft] = useState<any>(null);
  const [aiLoading, setAiLoading] = useState(false);
  const [award, setAward] = useState<Award | null>(null);
  const [readiness, setReadiness] = useState<Readiness | null>(null);
  const [itemCount, setItemCount] = useState(0);
  const [items, setItems] = useState<any[]>([]);
  const [quoteCount, setQuoteCount] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [approval, setApproval] = useState<Approval | null>(null);
  const [approving, setApproving] = useState(false);
  const [approvalMessage, setApprovalMessage] = useState("");
  const [showInviteModal, setShowInviteModal] = useState(false);
  const [selectedVendors, setSelectedVendors] = useState<string[]>(["EcoShip Cartons", "BoxCraft Solutions", "PrimeCorr", "CorruMax", "PackRight"]);
  const [invitationSent, setInvitationSent] = useState(false);
  const [invitationRecords, setInvitationRecords] = useState<{ rfxId: string; vendorName: string; email: string; sentAt: string }[]>([]);
  const mockVendors = [
    { name: "EcoShip Cartons", email: "sales@ec ship.example.com".replace(" ", "") },
    { name: "BoxCraft Solutions", email: "quotes@boxcraft.example.com" },
    { name: "PrimeCorr", email: "sales@primecorr.example.com" },
    { name: "CorruMax", email: "rfq@corrumax.example.com" },
    { name: "PackRight", email: "quotes@packright.example.com" },
  ];
  const handleSendInvitations = () => {
    if (!selectedVendors.length) return;
    const sentAt = new Date().toISOString();
    const records = mockVendors
      .filter((vendor) => selectedVendors.includes(vendor.name))
      .map((vendor) => ({ rfxId: selectedRfxId, vendorName: vendor.name, email: vendor.email, sentAt }));
    setInvitationRecords((prev) => [
      ...prev.filter((record) => !(record.rfxId === selectedRfxId && selectedVendors.includes(record.vendorName))),
      ...records,
    ]);
    setInvitationSent(true);
  };

  const handleApproveAward = async () => {
    try {
      setApproving(true);
      setApprovalMessage("");

      const response = await approveAward(selectedRfxId);

      setApproval(response.data);
      setApprovalMessage("Award approved successfully.");

      await loadDashboard();
    } catch (error: any) {
      setApprovalMessage(
        typeof error?.response?.data === "string"
          ? error.response.data
          : error?.response?.data?.message ||
          "Award approval blocked. Please check readiness."
      );
    } finally {
      setApproving(false);
    }
  };
  const loadDashboard = async () => {
    setLoading(true);
    setError("");

    try {
      const [awardRes, readinessRes, itemsRes, quotesRes] =
        await Promise.all([
          getAwardRecommendation(selectedRfxId),
          getAwardReadiness(selectedRfxId),
          getItems(selectedRfxId),
          getQuotes(selectedRfxId),
        ]);

      setAward(awardRes.data);
      setReadiness(readinessRes.data);

      const itemsData = itemsRes.data;
      const quotes = quotesRes.data;

      setItems(Array.isArray(itemsData) ? itemsData : []);
      setItemCount(Array.isArray(itemsData) ? itemsData.length : 0);
      setQuoteCount(Array.isArray(quotes) ? quotes.length : 0);
    } catch (err) {
      console.error(err);
      setError(
        "Unable to load dashboard. Check that Spring Boot is running on port 8080."
      );
    } finally {
      setLoading(false);
    }
    try {
      const approvalRes = await getAwardApproval(selectedRfxId);
      setApproval(approvalRes.data);
    } catch {
      setApproval(null);
    }
  };

  useEffect(() => {
    fetch("http://localhost:8080/api/rfx")
      .then((res) => { if (!res.ok) throw new Error("Unable to load RFx list"); return res.json(); })
      .then((data) => {
        const list = Array.isArray(data) ? data : [];
        setRfxList(list);
        if (list.length && !list.some((r) => r.rfxId === selectedRfxId)) setSelectedRfxId(list[0].rfxId);
      })
      .catch((err) => console.error("RFx list:", err));
  }, []);

  useEffect(() => {
    loadDashboard();
  }, [selectedRfxId]);


  const handleGenerateAiDraft = async () => {
    if (!aiPrompt.trim()) {
      setRfxError("Please describe what you want to procure.");
      return;
    }

    setAiLoading(true);
    setRfxError("");
    setAiDraft(null);

    try {
      const response = await fetch("http://localhost:8080/api/rfx/ai-draft", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ prompt: aiPrompt }),
      });

      if (!response.ok) {
        throw new Error(await response.text() || "AI generation failed");
      }

      const draft = await response.json();
      setAiDraft(draft);
      setNewRfxTitle(draft.title || "");
      setNewRfxDescription(draft.description || "");
      setNewRfxDeadline(draft.submissionDeadline || "");
    } catch (err: any) {
      setRfxError(err.message || "Could not generate RFx draft.");
    } finally {
      setAiLoading(false);
    }
  };


  const handleCreateRfx = async (e: React.FormEvent) => {
    e.preventDefault();
    setRfxError("");

    try {
      // 1. Create the RFx header
      const response = await fetch("http://localhost:8080/api/rfx", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          title: newRfxTitle,
          description: newRfxDescription,
          submissionDeadline: newRfxDeadline || null,
          createdBy: "Melvia",
        }),
      });

      if (!response.ok) {
        throw new Error(await response.text() || "Could not create RFx");
      }

      const created = await response.json();

      // 2. Save AI-generated line items
      if (aiDraft?.items?.length) {
        for (const item of aiDraft.items) {
          const itemResponse = await fetch(
            `http://localhost:8080/api/rfx/${created.rfxId}/items`,
            {
              method: "POST",
              headers: { "Content-Type": "application/json" },
              body: JSON.stringify(item),
            }
          );

          if (!itemResponse.ok) {
            throw new Error(
              `Failed to save item ${item.itemId}: ${await itemResponse.text()}`
            );
          }
        }
      }

      // 3. Update the UI
      setRfxList((prev) => [...prev, created]);
      setSelectedRfxId(created.rfxId);
      setShowCreateRfx(false);

      setNewRfxTitle("");
      setNewRfxDescription("");
      setNewRfxDeadline("");
      setAiPrompt("");
      setAiDraft(null);

    } catch (err: any) {
      setRfxError(err.message || "Could not create RFx.");
    }
  };


  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center text-slate-500">
        <RefreshCw className="animate-spin mr-3" />
        Loading QuotePilot...
      </div>
    );
  }

  if (error || !award || !readiness) {
    return (
      <div className="min-h-screen flex flex-col items-center justify-center gap-4">
        <AlertTriangle className="text-red-500" size={32} />
        <p className="text-slate-700">{error || "Dashboard data unavailable"}</p>
        <button
          onClick={loadDashboard}
          className="bg-indigo-600 text-white px-4 py-2 rounded-lg"
        >
          Retry
        </button>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-slate-50 text-slate-800 flex">
      <aside className="w-64 bg-white border-r border-slate-200 flex flex-col">
        <div className="p-6 border-b border-slate-100">
          <div className="flex items-center gap-3">
            <div className="bg-indigo-600 text-white rounded-xl p-2">
              <Package size={22} />
            </div>
            <div>
              <h1 className="text-xl font-bold">QuotePilot</h1>
              <p className="text-xs text-slate-500">Procurement intelligence</p>
            </div>
          </div>
        </div>

        <nav className="p-4 space-y-1">
          {[
            ["Overview", LayoutDashboard],
            ["RFx & Items", FileText],
            ["Vendor Quotes", Upload],
            ["Comparison", GitCompare],
            ["Award Recommendation", Trophy],
          ].map(([label, Icon], i) => {
            const I = Icon as typeof LayoutDashboard;
            return (
              <button
                key={label as string}
                onClick={() => setActivePage(label as string)}
                className={`w-full flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm ${activePage === label
                  ? "bg-indigo-50 text-indigo-700 font-medium"
                  : "text-slate-600 hover:bg-slate-50"
                  }`}
              >
                <I size={18} />
                {label as string}
              </button>
            );
          })}
        </nav>

        <div className="mt-auto p-4 border-t border-slate-100">
          <button className="flex items-center gap-3 text-sm text-slate-600 px-3 py-2">
            <Settings size={18} /> Settings
          </button>
          <div className="mt-4 bg-slate-50 rounded-xl p-3 flex items-center gap-3">
            <div className="h-9 w-9 rounded-full bg-indigo-100 text-indigo-700 flex items-center justify-center font-semibold">
              M
            </div>
            <div>
              <p className="text-sm font-medium">Melvia</p>
              <p className="text-xs text-slate-500">Category Buyer</p>
            </div>
          </div>
        </div>
      </aside>

      <main className="flex-1 min-w-0">
        <header className="h-16 bg-white border-b border-slate-200 px-8 flex items-center justify-between">
          <div className="text-sm text-slate-500">
            Procurement / <span className="text-slate-800 font-medium">{activePage}</span>
          </div>
          <div className="flex items-center gap-5">
            <Search size={19} className="text-slate-400" />
            <Bell size={19} className="text-slate-400" />
            <div className="flex items-center gap-2 text-sm font-medium">
              <div className="w-8 h-8 rounded-full bg-indigo-100 text-indigo-700 flex items-center justify-center">M</div>
              Melvia <ChevronDown size={15} />
            </div>
          </div>
        </header>

        {(activePage === "Overview" || activePage === "Award Recommendation") && (
          <div className="p-8 max-w-[1600px] mx-auto space-y-7">
            <div className="flex items-center justify-between gap-4 mb-5 rounded-xl border border-slate-200 bg-white p-4">
              <div className="flex items-center gap-3">
                <label className="text-sm font-medium text-slate-600">Active RFx</label>
                <select value={selectedRfxId} onChange={(e) => setSelectedRfxId(e.target.value)} className="rounded-lg border border-slate-300 px-3 py-2 text-sm min-w-56">
                  {rfxList.length === 0 && <option value={selectedRfxId}>{selectedRfxId}</option>}
                  {rfxList.map((rfx) => <option key={rfx.rfxId} value={rfx.rfxId}>{rfx.title} ({rfx.rfxId})</option>)}
                </select>
                <span className="text-xs text-slate-400">{selectedRfxId}</span>
              </div>
              <div className="flex gap-2">
                <button onClick={() => { setShowInviteModal(true); setInvitationSent(false); }} className="rounded-lg border border-indigo-200 bg-white px-4 py-2 text-sm font-medium text-indigo-700 hover:bg-indigo-50"><Mail size={15} className="inline mr-2" />Invite Vendors</button>
                <button onClick={() => setShowCreateRfx((v) => !v)} className="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700">+ New RFx</button>
              </div>
            </div>
            {showInviteModal && <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
              <div className="w-full max-w-lg rounded-2xl bg-white p-6 shadow-xl">
                <div className="flex items-center justify-between mb-2"><h3 className="text-lg font-semibold">Invite vendors</h3><button onClick={() => setShowInviteModal(false)} className="text-slate-500 hover:text-slate-800"><X size={20}/></button></div>
                <p className="text-sm text-slate-500 mb-4">RFx: {rfxList.find(r => r.rfxId === selectedRfxId)?.title || selectedRfxId}</p>
                {!invitationSent ? <>
                  <p className="text-sm font-medium mb-2">Select vendors to invite</p>
                  <div className="space-y-2 mb-5">{mockVendors.map(vendor => <label key={vendor.name} className="flex items-center gap-3 rounded-lg border p-3 text-sm"><input type="checkbox" checked={selectedVendors.includes(vendor.name)} onChange={e => setSelectedVendors(prev => e.target.checked ? [...prev, vendor.name] : prev.filter(n => n !== vendor.name))}/><span className="font-medium">{vendor.name}</span><span className="ml-auto text-slate-500">{vendor.email}</span></label>)}</div>
                  <button onClick={handleSendInvitations} disabled={!selectedVendors.length} className="w-full rounded-lg bg-indigo-600 px-4 py-2.5 text-sm font-medium text-white disabled:opacity-40"><Send size={15} className="inline mr-2"/>Send Invitations ({selectedVendors.length})</button>
                </> : <div className="rounded-xl bg-green-50 p-5 text-center"><CheckCircle2 size={30} className="mx-auto text-green-600 mb-2"/><p className="font-semibold text-green-800">Invitations sent successfully!</p><p className="text-sm text-green-700 mt-1">Simulated email sent to {selectedVendors.length} vendor(s).</p><p className="text-xs text-green-700 mt-3">No actual emails were delivered.</p><button onClick={() => setShowInviteModal(false)} className="mt-4 rounded-lg bg-green-700 px-4 py-2 text-sm text-white">Done</button></div>}
              </div>
            </div>}
            {invitationRecords.some((record) => record.rfxId === selectedRfxId) && (
              <section className="mb-5 rounded-xl border border-slate-200 bg-white p-5">
                <div className="flex items-center justify-between mb-3">
                  <h3 className="font-semibold">Vendor Invitations</h3>
                  <span className="rounded-full bg-green-50 px-2.5 py-1 text-xs font-medium text-green-700">
                    {invitationRecords.filter((record) => record.rfxId === selectedRfxId).length} sent
                  </span>
                </div>
                <div className="overflow-x-auto">
                  <table className="w-full text-sm">
                    <thead className="text-left text-xs uppercase text-slate-500">
                      <tr><th className="py-2">Vendor</th><th className="py-2">Email</th><th className="py-2">Status</th><th className="py-2">Sent at</th></tr>
                    </thead>
                    <tbody className="divide-y">
                      {invitationRecords.filter((record) => record.rfxId === selectedRfxId).map((record) => (
                        <tr key={`${record.rfxId}-${record.vendorName}`}>
                          <td className="py-3 font-medium">{record.vendorName}</td>
                          <td className="py-3 text-slate-500">{record.email}</td>
                          <td className="py-3"><span className="rounded-full bg-green-50 px-2 py-1 text-xs text-green-700">Simulated email sent</span></td>
                          <td className="py-3 text-slate-500">{new Date(record.sentAt).toLocaleString("en-IN")}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
                <p className="mt-2 text-xs text-slate-400">Demo records only; invitations are not persisted and emails are not delivered.</p>
              </section>
            )}
            {showCreateRfx && <form onSubmit={handleCreateRfx} className="mb-5 grid grid-cols-1 md:grid-cols-2 gap-3 rounded-xl border border-indigo-100 bg-indigo-50 p-4">
              <div className="md:col-span-2 rounded-xl border border-indigo-200 bg-white p-4">
                <div className="flex items-center gap-2 mb-3">
                  <Sparkles size={18} className="text-indigo-600" />
                  <h3 className="font-semibold text-indigo-900">Create RFx with AI</h3>
                </div>

                <textarea
                  value={aiPrompt}
                  onChange={(e) => setAiPrompt(e.target.value)}
                  placeholder="Describe what you want to procure. Example: I need 5000 corrugated shipping boxes, 5-ply, suitable for heavy industrial components."
                  className="w-full rounded-lg border px-3 py-3 text-sm min-h-24"
                />

                <button
                  type="button"
                  onClick={handleGenerateAiDraft}
                  disabled={aiLoading}
                  className="mt-3 rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white disabled:opacity-50"
                >
                  {aiLoading ? "Generating..." : "✨ Generate with AI"}
                </button>

                {aiDraft && (
                  <div className="mt-5 border-t border-slate-200 pt-5">
                    <h4 className="font-semibold text-lg mb-4">
                      AI Generated Draft — Review
                    </h4>

                    <label className="block text-sm font-medium mb-1">
                      Title
                    </label>
                    <input
                      value={newRfxTitle}
                      onChange={(e) => setNewRfxTitle(e.target.value)}
                      className="w-full rounded-lg border px-3 py-2 text-sm mb-3"
                    />

                    <label className="block text-sm font-medium mb-1">
                      Description
                    </label>
                    <textarea
                      value={newRfxDescription}
                      onChange={(e) => setNewRfxDescription(e.target.value)}
                      className="w-full rounded-lg border px-3 py-2 text-sm mb-3"
                    />

                    <label className="block text-sm font-medium mb-1">
                      Scope of Work
                    </label>
                    <textarea
                      value={aiDraft.scopeOfWork || ""}
                      onChange={(e) =>
                        setAiDraft({ ...aiDraft, scopeOfWork: e.target.value })
                      }
                      className="w-full rounded-lg border px-3 py-2 text-sm mb-3"
                    />

                    <h5 className="font-medium mb-2">
                      Line Items ({aiDraft.items?.length || 0})
                    </h5>

                    {aiDraft.items?.map((item: any, index: number) => (
                      <div key={item.itemId} className="rounded-lg border p-3 mb-2">
                        <p className="font-medium text-sm">{item.itemId}</p>
                        <p className="text-sm text-slate-600">{item.description}</p>
                        <p className="text-xs text-slate-500 mt-1">
                          Quantity: {item.requiredQty} {item.unit} · Ply: {item.ply ?? "Not specified"}
                        </p>
                      </div>
                    ))}
                  </div>
                )}

              </div>

              <input required value={newRfxTitle} onChange={(e) => setNewRfxTitle(e.target.value)} placeholder="RFx title" className="rounded-lg border px-3 py-2 text-sm" />
              <input type="date" value={newRfxDeadline} onChange={(e) => setNewRfxDeadline(e.target.value)} className="rounded-lg border px-3 py-2 text-sm" />
              <input value={newRfxDescription} onChange={(e) => setNewRfxDescription(e.target.value)} placeholder="Description (optional)" className="rounded-lg border px-3 py-2 text-sm md:col-span-2" />
              {rfxError && <p className="text-sm text-red-600 md:col-span-2">{rfxError}</p>}
              <div className="md:col-span-2 flex justify-end"><button type="submit" className="rounded-lg bg-indigo-600 px-4 py-2 text-sm text-white">Create RFx</button></div>
            </form>}
            <RfxItemsImport
              rfxId={selectedRfxId}
              onImported={loadDashboard}
            />
            <div className="flex justify-between items-start">
              <div>
                <span className="text-xs font-semibold bg-indigo-100 text-indigo-700 px-2.5 py-1 rounded-full">
                  LIVE RFx
                </span>
                <h2 className="text-2xl font-bold mt-3">
                  {activePage === "Overview" ? "Procurement Overview" : "Award Recommendation"}
                </h2>
                <p className="text-slate-500 mt-1">
                  {activePage === "Overview"
                    ? "A snapshot of your RFx progress and recommended award."
                    : "Review the allocation, readiness checks, and approve the award."}
                </p>
              </div>
              <button
                onClick={loadDashboard}
                className="border bg-white px-4 py-2 rounded-lg text-sm flex items-center gap-2 hover:bg-slate-50"
              >
                <RefreshCw size={15} /> Refresh
              </button>
            </div>

            <div className="grid grid-cols-4 gap-5">
              <Kpi title="RFx Line Items" value={award.totalItems} subtitle="Requested items" icon={<Package />} />
              <Kpi title="Awarded Items" value={award.awardedItems} subtitle={`${award.unawardedItems} unawarded`} icon={<Truck />} />
              <Kpi title="Quotes Received" value={quoteCount} subtitle="Quote records" icon={<FileText />} />
              <Kpi
                title="Award Readiness"
                value={readiness.readyForApproval ? "Ready" : "Blocked"}
                subtitle={`${readiness.passedChecks}/${readiness.totalChecks} checks passed`}
                icon={<ShieldCheck />}
                green={readiness.readyForApproval}
              />
            </div>

            {activePage === "Overview" && (
              <div className="grid grid-cols-3 gap-5">
                <section className="col-span-2 bg-white rounded-2xl border border-slate-200 p-6">
                  <div className="flex items-start justify-between gap-4">
                    <div>
                      <h3 className="font-semibold text-lg">Award recommendation</h3>
                      <p className="text-sm text-slate-500 mt-1">Lowest eligible price · Split award</p>
                    </div>
                    <span className={`text-xs px-3 py-1.5 rounded-full ${readiness.readyForApproval ? "text-green-700 bg-green-50" : "text-amber-700 bg-amber-50"}`}>
                      {readiness.readyForApproval ? "Ready for approval" : "Review required"}
                    </span>
                  </div>
                  <div className="grid grid-cols-3 gap-4 mt-6">
                    <Metric label="Quoted award" value={compactMoney(award.quotedAwardValueInr)} />
                    <Metric label="Additional freight" value={money(award.additionalFreightInr)} amber />
                    <Metric label="Total landed cost" value={compactMoney(award.totalLandedCostInr)} indigo />
                  </div>
                  <div className="mt-6 border-t border-slate-100 pt-4 flex justify-between text-sm">
                    <span className="text-slate-600">{award.awardedItems}/{award.totalItems} items awarded across {award.vendorSummaries.length} suppliers</span>
                    <span className="font-semibold">{money(award.totalAwardValueInr)}</span>
                  </div>
                  <button onClick={() => setActivePage("Award Recommendation")} className="mt-5 rounded-lg bg-indigo-600 px-4 py-2.5 text-sm font-medium text-white hover:bg-indigo-700">
                    Review Award Recommendation <span aria-hidden="true">→</span>
                  </button>
                </section>
                <section className="bg-white rounded-2xl border border-slate-200 p-6">
                  <div className="flex items-center justify-between">
                    <h3 className="font-semibold">Award readiness</h3>
                    <ShieldCheck className={readiness.readyForApproval ? "text-green-600" : "text-amber-600"} size={20} />
                  </div>
                  <p className={`text-2xl font-bold mt-5 ${readiness.readyForApproval ? "text-green-700" : "text-amber-700"}`}>
                    {readiness.readyForApproval ? "Ready" : "Blocked"}
                  </p>
                  <p className="text-sm text-slate-500 mt-1">{readiness.passedChecks}/{readiness.totalChecks} checks passed</p>
                  <p className={`mt-4 text-xs rounded-lg p-3 ${readiness.readyForApproval ? "bg-green-50 text-green-800" : "bg-amber-50 text-amber-800"}`}>
                    {readiness.readyForApproval ? "No unresolved issues. The award can proceed to approval." : `${readiness.unresolvedIssues} unresolved issue(s) need attention.`}
                  </p>
                </section>
              </div>
            )}

            {activePage === "Award Recommendation" && (
              <>
            <div className="grid grid-cols-3 gap-5">
              <section className="col-span-2 bg-white rounded-2xl border border-slate-200 p-6">
                <div className="flex justify-between items-center">
                  <div>
                    <h3 className="font-semibold text-lg">Award recommendation</h3>
                    <p className="text-sm text-slate-500 mt-1">Lowest eligible price · Split award</p>
                  </div>
                  <span className={`text-xs px-3 py-1.5 rounded-full flex items-center gap-1 ${readiness.readyForApproval
                    ? "text-green-700 bg-green-50"
                    : "text-amber-700 bg-amber-50"
                    }`}>
                    {readiness.readyForApproval ? <CheckCircle2 size={14} /> : <AlertTriangle size={14} />}
                    {readiness.readyForApproval ? "Ready for approval" : "Review required"}
                  </span>
                </div>

                <div className="grid grid-cols-3 gap-4 mt-6">
                  <Metric label="Quoted award" value={compactMoney(award.quotedAwardValueInr)} />
                  <Metric label="Additional freight" value={money(award.additionalFreightInr)} amber />
                  <Metric label="Total landed cost" value={compactMoney(award.totalLandedCostInr)} indigo />
                </div>

                <div className="mt-6 border-t border-slate-100 pt-4 flex justify-between text-sm">
                  <span className="text-slate-600 flex items-center gap-2">
                    <CheckCircle2 className="text-green-600" size={16} />
                    {award.awardedItems}/{award.totalItems} items awarded
                  </span>
                  <span className="font-semibold">{money(award.totalAwardValueInr)}</span>
                </div>
              </section>

              <section className="bg-white rounded-2xl border border-slate-200 p-6">
                <div className="flex items-center justify-between">
                  <h3 className="font-semibold">Award Readiness</h3>
                  <ShieldCheck className={readiness.readyForApproval ? "text-green-600" : "text-amber-600"} size={20} />
                </div>
                <div className="mt-5 space-y-4">
                  {readiness.passedChecksList.map((check) => (
                    <div key={check} className="flex gap-2.5 text-sm">
                      <CheckCircle2 size={17} className="text-green-600 shrink-0" />
                      {check}
                    </div>
                  ))}
                  {readiness.issues.map((issue) => (
                    <div key={issue} className="flex gap-2 text-sm text-amber-700">
                      <AlertTriangle size={17} /> {issue}
                    </div>
                  ))}
                </div>
                <div className={`mt-5 text-xs rounded-lg p-3 ${readiness.readyForApproval
                  ? "bg-green-50 text-green-800"
                  : "bg-amber-50 text-amber-800"
                  }`}>
                  {readiness.readyForApproval
                    ? "No unresolved issues. Recommendation can proceed to buyer approval."
                    : `${readiness.unresolvedIssues} unresolved issue(s) require attention.`}
                </div>
              </section>
              <section className="bg-white border border-slate-200 rounded-2xl p-6">
                <div className="flex items-center justify-between">
                  <div>
                    <h3 className="font-semibold text-lg">Buyer Approval</h3>
                    <p className="text-sm text-slate-500 mt-1">
                      Final approval of the recommended supplier allocation.
                    </p>
                  </div>

                  {approval?.status === "APPROVED" ? (
                    <span className="bg-green-50 text-green-700 px-3 py-1.5 rounded-full text-sm font-medium">
                      Approved
                    </span>
                  ) : (
                    <span className="bg-amber-50 text-amber-700 px-3 py-1.5 rounded-full text-sm font-medium">
                      Pending approval
                    </span>
                  )}
                </div>

                {approval?.status === "APPROVED" ? (
                  <div className="mt-5 bg-green-50 rounded-xl p-4">
                    <div className="flex items-center gap-2 text-green-800 font-medium">
                      <CheckCircle2 size={18} />
                      Award approved successfully
                    </div>

                    <p className="text-sm text-green-700 mt-2">
                      Approved value: {money(approval.approvedAwardValueInr)}
                    </p>

                    <p className="text-sm text-green-700">
                      Items awarded: {approval.awardedItems}
                    </p>

                    <p className="text-xs text-green-700 mt-2">
                      {approval.approvedAt
                        ? new Date(approval.approvedAt).toLocaleString("en-IN")
                        : ""}
                    </p>
                  </div>
                ) : (
                  <div className="mt-5">
                    <button
                      onClick={handleApproveAward}
                      disabled={!readiness.readyForApproval || approving}
                      className={`px-5 py-2.5 rounded-lg text-sm font-medium ${readiness.readyForApproval
                        ? "bg-indigo-600 text-white hover:bg-indigo-700"
                        : "bg-slate-200 text-slate-500 cursor-not-allowed"
                        }`}
                    >
                      {approving ? "Approving..." : "Approve Award"}
                    </button>

                    {!readiness.readyForApproval && (
                      <p className="text-sm text-amber-700 mt-2">
                        Resolve all readiness issues before approving.
                      </p>
                    )}
                  </div>
                )}

                {approvalMessage && (
                  <p className="text-sm mt-3 text-slate-600">
                    {approvalMessage}
                  </p>
                )}
              </section>
            </div>

            <section className="bg-white rounded-2xl border border-slate-200">
              <div className="p-6 border-b border-slate-100">
                <h3 className="font-semibold text-lg">Recommended supplier allocation</h3>
                <p className="text-sm text-slate-500 mt-1">
                  {award.vendorSummaries.length} suppliers · {award.awardedItems} line items
                </p>
              </div>

              <div className="overflow-x-auto">
                <table className="w-full text-sm">
                  <thead className="bg-slate-50 text-slate-500 text-xs uppercase">
                    <tr>
                      <th className="text-left px-6 py-4">Supplier</th>
                      <th className="text-right px-6 py-4">Items awarded</th>
                      <th className="text-right px-6 py-4">Quoted value</th>
                      <th className="text-right px-6 py-4">Share of award</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-100">
                    {award.vendorSummaries.map((vendor) => (
                      <tr key={vendor.vendorId} className="hover:bg-slate-50">
                        <td className="px-6 py-4">
                          <div className="font-medium">{vendor.vendorName}</div>
                          <div className="text-xs text-slate-400 mt-1">{vendor.vendorId}</div>
                        </td>
                        <td className="px-6 py-4 text-right">{vendor.itemsAwarded}</td>
                        <td className="px-6 py-4 text-right font-medium">{money(vendor.totalAwardValueInr)}</td>
                        <td className="px-6 py-4 text-right">
                          {award.quotedAwardValueInr
                            ? `${((vendor.totalAwardValueInr / award.quotedAwardValueInr) * 100).toFixed(1)}%`
                            : "—"}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
              <div className="px-6 py-4 bg-slate-50 text-xs text-slate-500">
                Supplier allocation values exclude freight. Freight is included separately in landed cost.
              </div>
            </section>

            <section className="bg-indigo-50 border border-indigo-100 rounded-2xl p-5 flex gap-4">
              <div className="bg-indigo-100 text-indigo-700 rounded-xl p-2.5 h-fit">
                <Sparkles size={20} />
              </div>
              <div>
                <h4 className="font-semibold text-indigo-900">QuotePilot insight</h4>
                <p className="text-sm text-indigo-800 mt-1">
                  The recommended award covers {award.awardedItems} of {award.totalItems} items
                  across {award.vendorSummaries.length} suppliers. Quoted value is{" "}
                  {money(award.quotedAwardValueInr)}, with {money(award.additionalFreightInr)}
                  {" "}additional freight, resulting in a landed cost of{" "}
                  {money(award.totalLandedCostInr)}.
                </p>
              </div>
            </section>

            <section className="bg-white border border-slate-200 rounded-2xl p-6">
              <h3 className="font-semibold mb-4">Award line items</h3>
              <div className="overflow-x-auto max-h-[420px]">
                <table className="w-full text-sm">
                  <thead className="sticky top-0 bg-slate-50 text-slate-500">
                    <tr>
                      <th className="text-left p-3">Item</th>
                      <th className="text-left p-3">Supplier</th>
                      <th className="text-right p-3">Quantity</th>
                      <th className="text-right p-3">Unit price</th>
                      <th className="text-right p-3">Line total</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y">
                    {award.awardLines.map((line) => (
                      <tr key={line.itemId} className="hover:bg-slate-50">
                        <td className="p-3 font-medium">{line.itemId}</td>
                        <td className="p-3">{line.vendorName}</td>
                        <td className="p-3 text-right">{line.quantity.toLocaleString("en-IN")}</td>
                        <td className="p-3 text-right">{money(line.unitPriceInr)}</td>
                        <td className="p-3 text-right font-medium">{money(line.totalPriceInr)}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </section>
            {/* Procurement Analyst */}
            <section className="bg-white border border-slate-200 rounded-2xl p-6">
              <div className="flex items-center gap-2 mb-4">
                <Sparkles className="text-indigo-600" size={20} />
                <div>
                  <h3 className="font-semibold text-lg">
                    AI Procurement Analyst
                  </h3>
                  <p className="text-sm text-slate-500">
                    Ask questions about supplier quotes, qualification and awards.
                  </p>
                </div>
              </div>

              <AnalystChat rfxId={selectedRfxId} />
            </section>
            </>
            )}

          </div>
        )}

        {activePage === "Comparison" && (
          <ComparisonPage
            rfxId={selectedRfxId}
            items={items}
          />
        )}

        {activePage === "RFx & Items" && (
          <div className="p-8 max-w-[1600px] mx-auto">
            <h2 className="text-2xl font-bold mb-2">RFx & Items</h2>
            <p className="text-slate-500 mb-6">
              Manage procurement requests and line items.
            </p>

            <div className="bg-white border rounded-xl p-6">
              <h3 className="font-semibold mb-4">Current RFx</h3>

              <p className="text-sm text-slate-600 mb-2">
                RFx ID: {selectedRfxId}
              </p>

              <p className="text-sm text-slate-600">
                Total Items: {items.length}
              </p>

              <div className="mt-4 overflow-x-auto">
                <table className="w-full text-sm">
                  <thead>
                    <tr className="border-b text-left">
                      <th className="p-3">Item ID</th>
                      <th className="p-3">Description</th>
                    </tr>
                  </thead>
                  <tbody>
                    {items.map((item) => (
                      <tr key={item.itemId} className="border-b">
                        <td className="p-3">{item.itemId}</td>
                        <td className="p-3">{item.description}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        )}

        {activePage === "Vendor Quotes" && (
          <div className="p-8 max-w-[1600px] mx-auto">
            <h2 className="text-2xl font-bold mb-2">
              Vendor Quotes
            </h2>

            <p className="text-slate-500 mb-6">
              Upload supplier quotations, review AI extraction, and confirm quote details.
            </p>

            <VendorQuoteUpload
              rfxId={selectedRfxId}
              items={items}
              onQuotesSaved={loadDashboard}
            />
          </div>
        )}

      </main>
    </div>
  );
}

function Kpi({ title, value, subtitle, icon, green = false }: {
  title: string;
  value: string | number;
  subtitle: string;
  icon: React.ReactNode;
  green?: boolean;
}) {
  return (
    <div className="bg-white rounded-2xl border border-slate-200 p-5">
      <div className="flex justify-between items-start">
        <p className="text-sm text-slate-500">{title}</p>
        <div className={`p-2 rounded-lg ${green ? "bg-green-50 text-green-600" : "bg-indigo-50 text-indigo-600"}`}>
          {icon}
        </div>
      </div>
      <p className={`text-2xl font-bold mt-3 ${green ? "text-green-700" : "text-slate-900"}`}>
        {value}
      </p>
      <p className="text-xs text-slate-400 mt-1">{subtitle}</p>
    </div>
  );
}

function Metric({ label, value, amber = false, indigo = false }: {
  label: string;
  value: string;
  amber?: boolean;
  indigo?: boolean;
}) {
  return (
    <div className={`rounded-xl p-4 ${amber ? "bg-amber-50" : indigo ? "bg-indigo-50" : "bg-slate-50"
      }`}>
      <p className={`text-xs ${amber ? "text-amber-700" : indigo ? "text-indigo-700" : "text-slate-500"
        }`}>{label}</p>
      <p className={`text-xl font-bold mt-2 ${amber ? "text-amber-800" : indigo ? "text-indigo-800" : "text-slate-900"
        }`}>{value}</p>
    </div>
  );
}

export default App;
