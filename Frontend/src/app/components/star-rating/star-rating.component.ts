import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-star-rating',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div *ngIf="!editable" class="star-rating" [attr.aria-label]="rating ? (rating + ' out of 5') : 'No rating'">
      <div class="stars-outer">
        <div class="stars-inner" [ngStyle]="{ width: calcWidth() + '%' }"></div>
      </div>
    </div>

    <div *ngIf="editable" class="star-picker" role="radiogroup" [attr.aria-label]="ariaLabel || 'Select rating'">
      <button type="button" class="star-btn" *ngFor="let n of stars" (click)="select(n)" (mouseenter)="hover = n" (mouseleave)="hover = 0" [attr.aria-checked]="(rating ?? 0) >= n" role="radio">
        <svg width="20" height="20" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg" [attr.fill]="(hover ? hover >= n : (rating ?? 0) >= n) ? '#FFC107' : '#E0E0E0'">
          <path d="M12 .587l3.668 7.431 8.2 1.192-5.934 5.788 1.402 8.175L12 18.896l-7.336 3.977 1.402-8.175L.132 9.21l8.2-1.192z"/>
        </svg>
      </button>
    </div>
  `,
  styles: [
    `
    .star-rating { display:inline-block; vertical-align:middle; }
    .stars-outer{ position:relative; display:inline-block; width:110px; height:20px; overflow:hidden }
    .stars-inner{ position:absolute; top:0; left:0; white-space:nowrap; overflow:hidden; width:0; height:100%; background:linear-gradient(90deg,#FFC107 0 100%); -webkit-background-clip:text; }
    /* draw five stars using background SVG for crisp rendering */
    .stars-outer, .stars-inner{
      background-repeat:repeat-x;
      background-size:20px 20px;
      background-image: url("data:image/svg+xml;utf8,\x3csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 51 48'%3e%3cpath fill='%23E0E0E0' d='m25,1 7,17 18,2-13,12 4,18-16-9-16,9 4-18L0,20l18-2z'/%3e%3c/svg%3e");
    }
    .stars-inner{ background-image: url("data:image/svg+xml;utf8,\x3csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 51 48'%3e%3cpath fill='%23FFC107' d='m25,1 7,17 18,2-13,12 4,18-16-9-16,9 4-18L0,20l18-2z'/%3e%3c/svg%3e"); }
    .star-picker{ display:inline-flex; gap:6px; align-items:center }
    .star-btn{ background:transparent; border:0; padding:0; cursor:pointer; line-height:0 }
    .star-btn:focus{ outline:2px solid rgba(0,0,0,0.12); border-radius:4px }
    `
  ]
})
export class StarRatingComponent {
  @Input() rating: number | null | undefined = 0;
  @Input() editable: boolean = false;
  @Input() ariaLabel?: string;
  @Output() ratingChange = new EventEmitter<number | null>();

  hover = 0;
  stars = [1,2,3,4,5];

  select(n: number) {
    this.rating = n;
    this.ratingChange.emit(this.rating);
  }

  calcWidth() {
    const r = Number(this.rating) || 0;
    const pct = Math.max(0, Math.min(5, r)) / 5 * 100;
    return pct;
  }
}
