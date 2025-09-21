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
  @Input() levels: any[] = [];
  @Input() durations: any[] = [];
  @Input() prices: any[] = [];
  @Input() features: any[] = [];
  @Input() onlyDiscounted: boolean = false;

  @Output() categoriesChange = new EventEmitter<any[]>();
  @Output() levelsChange = new EventEmitter<any[]>();
  @Output() durationsChange = new EventEmitter<any[]>();
  @Output() pricesChange = new EventEmitter<any[]>();
  @Output() featuresChange = new EventEmitter<any[]>();
  @Output() onlyDiscountedChange = new EventEmitter<boolean>();

  onDiscoverTopics() {
    console.log('Discover topics clicked');
  }

  toggleCategory(category: any) {
    category.selected = !category.selected;
    // emit the updated categories array so parent can react (fetch/filter)
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
