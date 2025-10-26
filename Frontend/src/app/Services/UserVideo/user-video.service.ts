import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface VideoDto {
  videoId?: number;
  id?: number;
  videoName?: string;
  title?: string;
  name?: string;
  duration?: number;
  durationSeconds?: number;
  [key: string]: any;
}

@Injectable({
  providedIn: 'root'
})
export class UserVideoService {
  private api = `${environment.apiUrl}`;

  constructor(private http: HttpClient) {}

  /**
   * Mark a video as watched for the current user
   */
  markVideoAsWatched(videoId: number): Observable<string> {
    return this.http.post<string>(`${this.api}/watchVideo`, { videoId }, { responseType: 'text' as 'json' });
  }

  /**
   * Unmark a video as watched for the current user
   */
  unmarkVideoAsWatched(videoId: number): Observable<string> {
    return this.http.request<string>('DELETE', `${this.api}/unWatchVideo`, {
      body: { videoId },
      responseType: 'text' as 'json'
    });
  }

  /**
   * Get all watched videos for a user
   */
  getWatchedVideos(userId: number): Observable<VideoDto[]> {
    const url = `${this.api}/watched/${userId}`;
    console.log('[UserVideoService] getWatchedVideos called. URL:', url);
    return this.http.get<VideoDto[]>(url);
  }
}
