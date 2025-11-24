import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router, ActivatedRoute } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ForumService } from '../../services';
import { ForumCategory, Tag } from '../../models';

@Component({
  selector: 'app-create-topic',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './create-topic.component.html',
  styleUrls: ['./create-topic.component.css'],
})
export class CreateTopicComponent implements OnInit {
  categories: ForumCategory[] = [];
  popularTags: Tag[] = [];

  title = '';
  content = '';
  selectedCategoryId: number | null = null;
  selectedTags: string[] = [];
  newTag = '';

  submitting = false;
  error: string | null = null;

  constructor(
    private forumService: ForumService,
    private router: Router,
    private route: ActivatedRoute,
  ) {}

  ngOnInit(): void {
    this.loadCategories();
    this.loadPopularTags();

    // Check for pre-selected category from query params
    this.route.queryParams.subscribe((params) => {
      if (params['category']) {
        this.selectedCategoryId = parseInt(params['category'], 10);
      }
    });
  }

  loadCategories(): void {
    this.forumService.getAllCategories().subscribe({
      next: (categories) => {
        this.categories = categories;
      },
      error: (err) => {
        console.error('Error loading categories:', err);
      },
    });
  }

  loadPopularTags(): void {
    this.forumService.getPopularTags(20).subscribe({
      next: (tags) => {
        this.popularTags = tags;
      },
      error: (err) => {
        console.error('Error loading tags:', err);
      },
    });
  }

  addTag(tagName: string): void {
    const tag = tagName.trim().toLowerCase();
    if (tag && !this.selectedTags.includes(tag)) {
      this.selectedTags.push(tag);
    }
    this.newTag = '';
  }

  removeTag(tag: string): void {
    this.selectedTags = this.selectedTags.filter((t) => t !== tag);
  }

  togglePopularTag(tagName: string): void {
    if (this.selectedTags.includes(tagName)) {
      this.removeTag(tagName);
    } else {
      this.addTag(tagName);
    }
  }

  onSubmit(): void {
    if (!this.validateForm()) {
      return;
    }

    this.submitting = true;
    this.error = null;

    const topicData = {
      title: this.title,
      content: this.content,
      categoryId: this.selectedCategoryId!,
      tags: this.selectedTags,
    };

    this.forumService.createTopic(topicData).subscribe({
      next: (topic) => {
        this.router.navigate(['/forum/topic', topic.slug]);
      },
      error: (err) => {
        this.error = 'Failed to create topic. Please try again.';
        this.submitting = false;
        console.error('Error creating topic:', err);
      },
    });
  }

  validateForm(): boolean {
    if (!this.title.trim()) {
      this.error = 'Please enter a title';
      return false;
    }

    if (this.title.length < 10) {
      this.error = 'Title must be at least 10 characters';
      return false;
    }

    if (!this.content.trim()) {
      this.error = 'Please enter content';
      return false;
    }

    if (this.content.length < 20) {
      this.error = 'Content must be at least 20 characters';
      return false;
    }

    if (!this.selectedCategoryId) {
      this.error = 'Please select a category';
      return false;
    }

    return true;
  }
}
