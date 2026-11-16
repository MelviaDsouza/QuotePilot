
import axios from "axios";

export const api = axios.create({
  baseURL: "http://localhost:8080",
  headers: {
    "Content-Type": "application/json",
  },
});

export const getVendors = () => api.get("/api/vendors");
export const getItems = (rfxId: string) =>
  api.get(`/api/rfx/${rfxId}/items`);
export const getQuotes = (rfxId: string) =>
  api.get(`/api/rfx/${rfxId}/quotes`);
export const getAwardRecommendation = (rfxId: string) =>
  api.get(`/api/rfx/${rfxId}/award/recommendation`);
export const getAwardReadiness = (rfxId: string) =>
  api.get(`/api/rfx/${rfxId}/award/readiness`);
export const approveAward = (rfxId: string) =>
  api.post(`/api/rfx/${rfxId}/award/approve`);
export const getAwardApproval = (rfxId: string) =>
  api.get(`/api/rfx/${rfxId}/award/approval`);
export const getAllRfxs = () => api.get("/api/rfx");

export const createRfx = (data: {
  title: string;
  description: string;
  submissionDeadline: string;
  createdBy: string;
}) => api.post("/api/rfx", data);
export const getComparison = (rfxId: string, itemIds: string[]) =>
  api.post(`/api/rfx/${rfxId}/comparison/all`, itemIds);