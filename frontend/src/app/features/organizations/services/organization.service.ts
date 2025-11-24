import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import {
  Organization,
  OrganizationMember,
  CreateOrganizationRequest,
  UpdateOrganizationRequest,
  InviteMemberRequest,
  SsoConfig,
  ApiKeys,
} from '../models/organization.models';

@Injectable({
  providedIn: 'root',
})
export class OrganizationService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/organizations`;

  createOrganization(data: CreateOrganizationRequest): Observable<Organization> {
    return this.http.post<Organization>(this.apiUrl, data);
  }

  getOrganization(id: number): Observable<Organization> {
    return this.http.get<Organization>(`${this.apiUrl}/${id}`);
  }

  getUserOrganizations(): Observable<{ organizations: Organization[] }> {
    return this.http.get<{ organizations: Organization[] }>(`${this.apiUrl}/user/me`);
  }

  updateOrganization(id: number, data: UpdateOrganizationRequest): Observable<any> {
    return this.http.patch(`${this.apiUrl}/${id}`, data);
  }

  deleteOrganization(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`);
  }

  getMembers(id: number): Observable<{ members: OrganizationMember[] }> {
    return this.http.get<{ members: OrganizationMember[] }>(`${this.apiUrl}/${id}/members`);
  }

  inviteMember(id: number, data: InviteMemberRequest): Observable<any> {
    return this.http.post(`${this.apiUrl}/${id}/members`, data);
  }

  removeMember(id: number, memberId: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}/members/${memberId}`);
  }

  updateMemberRole(id: number, memberId: number, role: string, permissions?: string[]): Observable<any> {
    return this.http.patch(`${this.apiUrl}/${id}/members/${memberId}`, { role, permissions });
  }

  configureSso(id: number, data: SsoConfig): Observable<any> {
    return this.http.post(`${this.apiUrl}/${id}/sso`, data);
  }

  generateApiKeys(id: number): Observable<ApiKeys> {
    return this.http.post<ApiKeys>(`${this.apiUrl}/${id}/api-keys/generate`, {});
  }
}
