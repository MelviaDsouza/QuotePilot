import { useState } from "react";
import ReactMarkdown from "react-markdown";
import { API_BASE_URL } from "../services/api";

interface Message {
  role: "user" | "assistant";
  content: string;
}

interface AnalystChatProps {
  rfxId: string;
}

const suggestedQuestions = [
  "Which suppliers are disqualified and why?",
  "Who is the cheapest eligible supplier for BOX-01?",
  "Compare eligible and disqualified suppliers for BOX-01.",
  "Which supplier has additional freight?",
];

export default function AnalystChat({ rfxId }: AnalystChatProps) {
  const [messages, setMessages] = useState<Message[]>([]);
  const [question, setQuestion] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const sendMessage = async (text: string) => {
    const trimmed = text.trim();

    if (!trimmed || loading) return;

    const userMessage: Message = {
      role: "user",
      content: trimmed,
    };

    setMessages((prev) => [...prev, userMessage]);
    setQuestion("");
    setLoading(true);
    setError("");

    try {
      const response = await fetch(
        `${API_BASE_URL}/api/rfx/${rfxId}/analyst/chat`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            question: trimmed,
          }),
        }
      );

      if (!response.ok) {
        throw new Error(
          `Analyst request failed (${response.status})`
        );
      }

      const data = await response.json();

      setMessages((prev) => [
        ...prev,
        {
          role: "assistant",
          content: data.answer,
        },
      ]);
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : "Something went wrong."
      );
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    sendMessage(question);
  };

  return (
    <div className="flex flex-col h-[650px] rounded-xl border bg-white shadow-sm">

      {/* Header */}
      <div className="border-b px-5 py-4">
        <div className="flex items-center gap-3">
          <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-indigo-100 text-indigo-700 font-bold">
            AI
          </div>

          <div>
            <h2 className="font-semibold text-gray-900">
              Procurement Analyst
            </h2>
            <p className="text-sm text-gray-500">
              Ask questions about quotes, suppliers and awards
            </p>
          </div>
        </div>
      </div>

      {/* Chat messages */}
      <div className="flex-1 overflow-y-auto p-5 space-y-5">

        {messages.length === 0 && (
          <div className="py-8 text-center">
            <h3 className="text-lg font-semibold text-gray-800">
              How can I help with your RFx?
            </h3>

            <p className="mt-2 text-sm text-gray-500">
              Ask questions about supplier quotes, qualification
              or award decisions.
            </p>

            <div className="mt-6 grid gap-2 text-left">
              {suggestedQuestions.map((q) => (
                <button
                  key={q}
                  onClick={() => sendMessage(q)}
                  className="rounded-lg border px-4 py-3 text-sm text-gray-700 hover:border-indigo-300 hover:bg-indigo-50 transition"
                >
                  {q}
                </button>
              ))}
            </div>
          </div>
        )}

        {messages.map((message, index) => (
          <div
            key={index}
            className={`flex ${
              message.role === "user"
                ? "justify-end"
                : "justify-start"
            }`}
          >
            <div
              className={`max-w-[85%] rounded-xl px-4 py-3 text-sm ${
                message.role === "user"
                  ? "bg-indigo-600 text-white"
                  : "bg-gray-100 text-gray-800"
              }`}
            >
              {message.role === "assistant" ? (
                <div className="prose prose-sm max-w-none prose-table:text-xs">
                  <ReactMarkdown>
                    {message.content}
                  </ReactMarkdown>
                </div>
              ) : (
                <p className="whitespace-pre-wrap">
                  {message.content}
                </p>
              )}
            </div>
          </div>
        ))}

        {loading && (
          <div className="flex justify-start">
            <div className="rounded-xl bg-gray-100 px-4 py-3 text-sm text-gray-500">
              <span className="animate-pulse">
                Analysing procurement data...
              </span>
            </div>
          </div>
        )}

        {error && (
          <div className="rounded-lg bg-red-50 px-4 py-3 text-sm text-red-600">
            {error}
          </div>
        )}
      </div>

      {/* Input */}
      <form
        onSubmit={handleSubmit}
        className="border-t p-4"
      >
        <div className="flex gap-2">
          <input
            value={question}
            onChange={(e) => setQuestion(e.target.value)}
            placeholder="Ask your procurement analyst..."
            disabled={loading}
            className="flex-1 rounded-lg border px-4 py-3 text-sm outline-none focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500"
          />

          <button
            type="submit"
            disabled={loading || !question.trim()}
            className="rounded-lg bg-indigo-600 px-5 py-3 text-sm font-medium text-white hover:bg-indigo-700 disabled:opacity-50"
          >
            Send
          </button>
        </div>

        <p className="mt-2 text-xs text-gray-400">
          AI-generated analysis. Verify important award decisions
          against source quotes.
        </p>
      </form>
    </div>
  );
}
