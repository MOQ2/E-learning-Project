import { Input, Output, EventEmitter, isStandalone, Component } from '@angular/core';
import { CommonModule } from '@angular/common';
@Component({
  selector: 'app-sidebar',
  templateUrl: './sidebar.html',
  styleUrls: ['./sidebar.css'],
  standalone: true,
  imports: [CommonModule]
})
export class SidebarComponent {
  @Input() categories: any[] = [];
  @Input() levels: { name: string, value: string, selected?: boolean }[] = [
    { name: 'Beginner', value: 'biginner', selected: false },
    { name: 'Intermediate', value: 'intermediate', selected: false },
    { name: 'Advanced', value: 'advanced', selected: false }
  ];
  @Input() durations: { name: string, minDurationHours: number, maxDurationHours: number, selected?: boolean }[] = [
    { name: 'Less than 2 hours', minDurationHours: 0, maxDurationHours: 2, selected: false },
    { name: '2-5 hours', minDurationHours: 2, maxDurationHours: 5, selected: false },
    { name: '5-10 hours', minDurationHours: 5, maxDurationHours: 10, selected: false },
    { name: '10-20 hours', minDurationHours: 10, maxDurationHours: 20, selected: false }
  ];
  @Input() prices: { name: string, minPrice: number, maxPrice: number, selected?: boolean }[] = [
    { name: 'Free', minPrice: 0, maxPrice: 0, selected: false },
    { name: 'Less than $20', minPrice: 0.01, maxPrice: 20, selected: false },
    { name: 'Less than $40', minPrice: 20.01, maxPrice: 40, selected: false },
    { name: 'Less than $60', minPrice: 40.01, maxPrice: 60, selected: false }
  ];
  @Input() features: any[] = [];
  @Input() onlyDiscounted: boolean = false;

  @Output() categoriesChange = new EventEmitter<any[]>();
  @Output() levelsChange = new EventEmitter<any[]>();
  @Output() durationsChange = new EventEmitter<any[]>();
  @Output() pricesChange = new EventEmitter<any[]>();
  @Output() featuresChange = new EventEmitter<any[]>();
  @Output() onlyDiscountedChange = new EventEmitter<boolean>();
  showCategories = true;
  onDiscoverTopics() {
    console.log('Discover topics clicked');
  }

  toggleCategory(category: any) {
    category.selected = !category.selected;
    this.categoriesChange.emit(this.categories);
  }

  toggleLevel(level: any) {
    level.selected = !level.selected;
    this.levelsChange.emit(this.levels);
  }

  toggleDuration(duration: any) {
    duration.selected = !duration.selected;
    this.durationsChange.emit(this.durations);
  }

  togglePrice(price: any) {
    price.selected = !price.selected;
    this.pricesChange.emit(this.prices);
  }

  toggleFeature(feature: any) {
    feature.selected = !feature.selected;
    this.featuresChange.emit(this.features);
  }

  toggleOnlyDiscounted() {
    this.onlyDiscounted = !this.onlyDiscounted;
    this.onlyDiscountedChange.emit(this.onlyDiscounted);
  }
}
