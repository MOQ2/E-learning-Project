import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';

@Component({
  selector: 'app-list-card',
  standalone: true,
  imports: [ CommonModule ],
  templateUrl: './list-card.html',
  styleUrl: './list-card.css'
})

export class ListCard {
  @Input() id?: number;
  @Input() icon?: string;
  @Input() text?: string;
  @Input() subtitle?: string;
  @Input() rightText?: string;
  @Input() label?: string;
  @Input() labelColor: string = '#10B981';
  @Input() selected: boolean = false;
  @Input() maxWidth?: string;
  @Input() labelIcon: string | null = null;
  @Output() cardClick = new EventEmitter<number>();

  constructor(private sanitizer: DomSanitizer) {}

  handleClick(): void {
    if (this.id) {
      this.cardClick.emit(this.id);
    }
  }

  getButtonClasses(): string {
    return this.selected ? 'border-selected' : 'border-default';
  }

  getSanitizedLabelIcon(): SafeHtml | null {
    if (!this.labelIcon) return null;
    return this.sanitizer.bypassSecurityTrustHtml(this.labelIcon);
  }

  /**
   * Returns true if the provided icon string looks like a URL (http(s) or relative file path ending with .svg/.png/.jpg)
   */
  isUrl(value?: string): boolean {
    if (!value) return false;
    // basic heuristic: starts with http/https or with / or ./ or ../ or ends with common image extensions
    const startsWithUrl = /^(https?:)?\/\//i.test(value) || /^\.\.?\//.test(value) || /^\//.test(value);
    const hasImageExt = /\.(svg|png|jpg|jpeg|gif|webp)(\?.*)?$/i.test(value);
    return startsWithUrl || hasImageExt;
  }
}
