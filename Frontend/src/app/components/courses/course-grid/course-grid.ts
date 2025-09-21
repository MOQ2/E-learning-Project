import { Component } from '@angular/core';
import { CourseCardComponent } from '../course-card/course-card';
import { CommonModule } from '@angular/common';

export interface Course {
  id: number;
  title: string;
  instructor: string;
  lessons: number;
  duration: string;
  rating: number;
  students: string;
  category: string;
  image: string;
  categoryColor: string;
  isEnrolled?: boolean;
}

@Component({
  selector: 'app-course-grid',
  templateUrl: './course-grid.html',
  styleUrls: ['./course-grid.css'],
  imports: [CourseCardComponent , CommonModule],
  standalone: true
})
export class CourseGridComponent {
  currentPage = 1;
  totalPages : number = 5;
  totalResults : number = 124;


  topFilters = [
    { name: 'Trending', active: true, icon: '📈' },
    { name: 'Top Rated', active: false, icon: '⭐' },
    { name: 'New', active: false, icon: '🆕' },
    { name: 'Under 2h', active: false, icon: '⏰' }
  ];

  sortOptions = [
    { name: 'Popular', selected: true },
    { name: 'Newest', selected: false },
    { name: 'Rating', selected: false },
    { name: 'Duration', selected: false }
  ];

  courses: Course[] = [
    {
      id: 1,
      title: 'Full-Stack Web Dev Bootcamp',
      instructor: 'Sarah Lin',
      lessons: 48,
      duration: '12h',
      rating: 4.8,
      students: '23k',
      category: 'Development',
      categoryColor: '#22c55e',
      image: 'https://images.unsplash.com/photo-1461749280684-dccba630e2f6?w=400&h=200&fit=crop'
    },
    {
      id: 2,
      title: 'UI Design with Figma',
      instructor: 'Priya Desai',
      lessons: 26,
      duration: '8h',
      rating: 4.7,
      students: '12k',
      category: 'Design',
      categoryColor: '#3b82f6',
      image: 'https://images.unsplash.com/photo-1581291518857-4e27b48ff24e?w=400&h=200&fit=crop'
    },
    {
      id: 3,
      title: 'Python for Data Science',
      instructor: 'Liam Chen',
      lessons: 32,
      duration: '10h',
      rating: 4.9,
      students: '18k',
      category: 'Data',
      categoryColor: '#f59e0b',
      image: 'https://images.unsplash.com/photo-1551288049-bebda4e38f71?w=400&h=200&fit=crop'
    },
    {
      id: 4,
      title: 'Startup Strategy Essentials',
      instructor: 'Marco Ruiz',
      lessons: 18,
      duration: '5h',
      rating: 4.6,
      students: '9k',
      category: 'Business',
      categoryColor: '#8b5cf6',
      image: 'https://images.unsplash.com/photo-1556761175-b413da4baf72?w=400&h=200&fit=crop'
    },
    {
      id: 5,
      title: 'Spanish for Beginners',
      instructor: 'Ana Gomez',
      lessons: 24,
      duration: '6h',
      rating: 4.5,
      students: '7k',
      category: 'Language',
      categoryColor: '#ef4444',
      image: 'https://images.unsplash.com/photo-1434030216411-0b793f4b4173?w=400&h=200&fit=crop'
    },
    {
      id: 6,
      title: 'Introduction to Machine Learning',
      instructor: 'Noor Al-Khaled',
      lessons: 28,
      duration: '9h',
      rating: 4.8,
      students: '15k',
      category: 'AI',
      categoryColor: '#06b6d4',
      image: 'https://images.unsplash.com/photo-1555949963-aa79dcee981c?w=400&h=200&fit=crop'
    }
  ];

  onSurpriseMe() {
    console.log('Surprise me clicked');
  }

  onRefineFilters() {
    console.log('Refine filters clicked');
  }

  toggleTopFilter(filter: any) {
    this.topFilters.forEach(f => f.active = false);
    filter.active = true;
  }

  onMoreFilters() {
    console.log('More filters clicked');
  }

  onSortChange(option: any) {
    this.sortOptions.forEach(o => o.selected = false);
    option.selected = true;
  }

  goToPage(page: number) {
    if (page >= 1 && page <= this.totalPages) {
      this.currentPage = page;
    }
  }

  getPages(): number[] {
    return Array.from({ length: this.totalPages }, (_, i) => i + 1);
  }
}
