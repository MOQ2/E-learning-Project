import { Injectable } from '@angular/core';
import { UserVideoService } from '../UserVideo/user-video.service';

export interface VideoProgressConfig {
  videoElement: HTMLMediaElement;
  lessonId: number;
  userId: number;
  completionThreshold?: number; // Default 90%
  onProgressUpdate?: (progress: number) => void;
  onCompletion?: (lessonId: number) => void;
}

@Injectable({
  providedIn: 'root'
})
export class VideoProgressService {
  private watchedVideoIds: Set<number> = new Set();

  constructor(private userVideoService: UserVideoService) {}

  /**
   * Initialize video progress tracking
   * @param config Configuration for video progress tracking
   * @returns Cleanup function to remove event listeners
   */
  initializeProgressTracking(config: VideoProgressConfig): () => void {
    const {
      videoElement,
      lessonId,
      userId,
      completionThreshold = 90,
      onProgressUpdate,
      onCompletion
    } = config;

    if (!videoElement || !lessonId || !userId) {
      console.warn('[VideoProgressService] Missing required parameters:', {
        videoElement: !!videoElement,
        lessonId,
        userId
      });
      return () => {};
    }

    console.log('[VideoProgressService] Initializing progress tracking for lesson:', lessonId);

    // Remove existing listener if any
    const oldListener = (videoElement as any)._progressListener;
    if (oldListener) {
      videoElement.removeEventListener('timeupdate', oldListener);
      console.log('[VideoProgressService] Removed old progress listener');
    }

    // Track video progress and mark as completed when threshold is reached
    const progressListener = () => {
      if (videoElement.duration && videoElement.currentTime) {
        const progress = (videoElement.currentTime / videoElement.duration) * 100;

        // Log progress every 10% for debugging
        const progressRounded = Math.floor(progress / 10) * 10;
        if (!(videoElement as any)._lastLoggedProgress || (videoElement as any)._lastLoggedProgress !== progressRounded) {
          (videoElement as any)._lastLoggedProgress = progressRounded;
          console.log(`[VideoProgressService] Video progress: ${progress.toFixed(1)}% (${videoElement.currentTime.toFixed(1)}s / ${videoElement.duration.toFixed(1)}s)`);
        }

        // Notify progress update callback
        if (onProgressUpdate) {
          onProgressUpdate(progress);
        }

        // Mark as completed when threshold is reached
        if (progress >= completionThreshold) {
          // Only mark once per video session
          if (!(videoElement as any)._markedComplete) {
            (videoElement as any)._markedComplete = true;
            console.log(`[VideoProgressService] 🎯 Video ${progress.toFixed(1)}% watched. Marking lesson ${lessonId} as completed.`);
            this.markLessonAsCompleted(lessonId, userId, onCompletion);
          }
        }
      } else {
        // Log when duration/currentTime are not available
        if (!(videoElement as any)._loggedNoData) {
          (videoElement as any)._loggedNoData = true;
          console.warn('[VideoProgressService] Video duration or currentTime not available yet', {
            duration: videoElement.duration,
            currentTime: videoElement.currentTime
          });
        }
      }
    };

    // Store listener reference for cleanup
    (videoElement as any)._progressListener = progressListener;
    videoElement.addEventListener('timeupdate', progressListener);
    console.log('[VideoProgressService] ✓ Added timeupdate event listener');

    // Reset completion flag when video loads
    const metadataListener = () => {
      (videoElement as any)._markedComplete = false;
      (videoElement as any)._loggedNoData = false;
      (videoElement as any)._lastLoggedProgress = null;
      console.log('[VideoProgressService] Video metadata loaded, duration:', videoElement.duration);
    };

    videoElement.addEventListener('loadedmetadata', metadataListener);

    // Return cleanup function
    return () => {
      videoElement.removeEventListener('timeupdate', progressListener);
      videoElement.removeEventListener('loadedmetadata', metadataListener);
      console.log('[VideoProgressService] Cleaned up event listeners for lesson:', lessonId);
    };
  }

  /**
   * Mark lesson as completed in backend
   */
  private async markLessonAsCompleted(
    lessonId: number,
    userId: number,
    onCompletion?: (lessonId: number) => void
  ): Promise<void> {
    console.log('[VideoProgressService] markLessonAsCompleted called:', {
      lessonId,
      userId,
      alreadyWatched: this.watchedVideoIds.has(lessonId)
    });

    if (!userId || !lessonId) {
      console.warn('[VideoProgressService] ❌ Cannot mark lesson as completed: missing userId or lessonId', {
        userId,
        lessonId
      });
      return;
    }

    // Check if already marked to avoid duplicate requests
    if (this.watchedVideoIds.has(lessonId)) {
      console.log('[VideoProgressService] ℹ️ Lesson already marked as completed:', lessonId);
      return;
    }

    try {
      console.log('[VideoProgressService] 📤 Calling backend API to mark video as watched:', lessonId);
      // Call backend API to mark as watched
      const response = await this.userVideoService.markVideoAsWatched(lessonId).toPromise();
      console.log('[VideoProgressService] 📥 Backend API response:', response);
      console.log('[VideoProgressService] ✓ Successfully marked lesson as completed in backend:', lessonId);

      // Update local state
      this.watchedVideoIds.add(lessonId);
      console.log('[VideoProgressService] Updated watchedVideoIds. Total watched:', this.watchedVideoIds.size);

      // Notify completion callback
      if (onCompletion) {
        onCompletion(lessonId);
      }
    } catch (error) {
      console.error('[VideoProgressService] ❌ Failed to mark lesson as completed:', error);
    }
  }

  /**
   * Load watched videos for a user
   */
  async loadWatchedVideos(userId: number): Promise<Set<number>> {
    if (!userId) {
      console.warn('[VideoProgressService] Cannot load watched videos: missing userId');
      return new Set();
    }

    try {
      const watchedVideos = await this.userVideoService.getWatchedVideos(userId).toPromise();
      if (watchedVideos) {
        this.watchedVideoIds = new Set(
          watchedVideos.map(v => v.videoId || v.id).filter(id => id !== undefined) as number[]
        );
        console.log('[VideoProgressService] Loaded watched videos:', this.watchedVideoIds.size);
      }
      return this.watchedVideoIds;
    } catch (err) {
      console.warn('[VideoProgressService] Could not load watched videos:', err);
      return new Set();
    }
  }

  /**
   * Get watched video IDs
   */
  getWatchedVideoIds(): Set<number> {
    return this.watchedVideoIds;
  }

  /**
   * Check if a video is watched
   */
  isVideoWatched(videoId: number): boolean {
    return this.watchedVideoIds.has(videoId);
  }

  /**
   * Add a video to watched list (for manual marking)
   */
  addToWatchedList(videoId: number): void {
    this.watchedVideoIds.add(videoId);
  }

  /**
   * Clear watched videos cache
   */
  clearWatchedVideosCache(): void {
    this.watchedVideoIds.clear();
  }
}
