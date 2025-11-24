import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ForumService } from '../../services';
import { ForumTopic, ForumPost, LikeableType } from '../../models';

@Component({
  selector: 'app-forum-topic-view',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './forum-topic-view.component.html',
  styleUrls: ['./forum-topic-view.component.css'],
})
export class ForumTopicViewComponent implements OnInit {
  topic: ForumTopic | null = null;
  posts: ForumPost[] = [];
  loading = true;
  error: string | null = null;

  // Pagination
  currentPage = 1;
  totalPages = 1;
  totalPosts = 0;
  pageSize = 10;

  // New post
  newPostContent = '';
  replyingToPost: ForumPost | null = null;
  submittingPost = false;

  // Edit
  editingPost: ForumPost | null = null;
  editContent = '';

  // Likes
  userLikedTopic = false;
  userLikedPosts: Set<number> = new Set();

  // Current user (mock - should come from auth service)
  currentUserId: number | null = 1; // TODO: Get from auth service

  constructor(
    private forumService: ForumService,
    private route: ActivatedRoute,
    private router: Router,
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe((params) => {
      const slug = params['slug'];
      if (slug) {
        this.loadTopic(slug);
      }
    });
  }

  loadTopic(slug: string): void {
    this.loading = true;
    this.error = null;

    this.forumService.getTopicBySlug(slug).subscribe({
      next: (topic) => {
        this.topic = topic;
        this.loadPosts();
        this.checkUserLikes();
      },
      error: (err) => {
        this.error = 'Failed to load topic';
        this.loading = false;
        console.error('Error loading topic:', err);
      },
    });
  }

  loadPosts(): void {
    if (!this.topic) return;

    this.forumService
      .getPostsByTopic(this.topic.id, this.currentPage, this.pageSize)
      .subscribe({
        next: (response) => {
          this.posts = response.data;
          this.totalPosts = response.total;
          this.totalPages = Math.ceil(response.total / this.pageSize);
          this.loading = false;
        },
        error: (err) => {
          this.error = 'Failed to load posts';
          this.loading = false;
          console.error('Error loading posts:', err);
        },
      });
  }

  checkUserLikes(): void {
    if (!this.topic || !this.currentUserId) return;

    // Check topic like
    this.forumService
      .getUserLikes(LikeableType.TOPIC, [this.topic.id])
      .subscribe({
        next: (likedIds) => {
          this.userLikedTopic = likedIds.includes(this.topic!.id);
        },
      });

    // Check post likes
    if (this.posts.length > 0) {
      const postIds = this.posts.map((p) => p.id);
      this.forumService.getUserLikes(LikeableType.POST, postIds).subscribe({
        next: (likedIds) => {
          this.userLikedPosts = new Set(likedIds);
        },
      });
    }
  }

  onSubmitPost(): void {
    if (!this.newPostContent.trim() || !this.topic || this.submittingPost) {
      return;
    }

    this.submittingPost = true;

    const postData = {
      content: this.newPostContent,
      topicId: this.topic.id,
      replyToId: this.replyingToPost?.id,
    };

    this.forumService.createPost(postData).subscribe({
      next: () => {
        this.newPostContent = '';
        this.replyingToPost = null;
        this.submittingPost = false;
        this.loadPosts();
      },
      error: (err) => {
        alert('Failed to post reply. Please try again.');
        this.submittingPost = false;
        console.error('Error creating post:', err);
      },
    });
  }

  onReply(post: ForumPost): void {
    this.replyingToPost = post;
    this.newPostContent = '';
    // Scroll to reply box
    setTimeout(() => {
      document.querySelector('.reply-box')?.scrollIntoView({ behavior: 'smooth' });
    }, 100);
  }

  cancelReply(): void {
    this.replyingToPost = null;
  }

  onEdit(post: ForumPost): void {
    this.editingPost = post;
    this.editContent = post.content;
  }

  cancelEdit(): void {
    this.editingPost = null;
    this.editContent = '';
  }

  saveEdit(): void {
    if (!this.editingPost || !this.editContent.trim()) return;

    this.forumService
      .updatePost(this.editingPost.id, { content: this.editContent })
      .subscribe({
        next: () => {
          this.editingPost = null;
          this.editContent = '';
          this.loadPosts();
        },
        error: (err) => {
          alert('Failed to update post. Please try again.');
          console.error('Error updating post:', err);
        },
      });
  }

  onDelete(post: ForumPost): void {
    if (!confirm('Are you sure you want to delete this post?')) return;

    this.forumService.deletePost(post.id).subscribe({
      next: () => {
        this.loadPosts();
      },
      error: (err) => {
        alert('Failed to delete post. Please try again.');
        console.error('Error deleting post:', err);
      },
    });
  }

  toggleTopicLike(): void {
    if (!this.topic) return;

    this.forumService.toggleLike(LikeableType.TOPIC, this.topic.id).subscribe({
      next: (response) => {
        this.userLikedTopic = response.liked;
        this.topic!.likesCount = response.likesCount;
      },
      error: (err) => {
        console.error('Error toggling like:', err);
      },
    });
  }

  togglePostLike(post: ForumPost): void {
    this.forumService.toggleLike(LikeableType.POST, post.id).subscribe({
      next: (response) => {
        if (response.liked) {
          this.userLikedPosts.add(post.id);
        } else {
          this.userLikedPosts.delete(post.id);
        }
        post.likesCount = response.likesCount;
      },
      error: (err) => {
        console.error('Error toggling like:', err);
      },
    });
  }

  markBestAnswer(post: ForumPost): void {
    this.forumService.markBestAnswer(post.id).subscribe({
      next: () => {
        this.loadPosts();
      },
      error: (err) => {
        alert('Failed to mark best answer. Please try again.');
        console.error('Error marking best answer:', err);
      },
    });
  }

  isAuthor(authorId: number): boolean {
    return this.currentUserId === authorId;
  }

  isTopicAuthor(): boolean {
    return this.currentUserId === this.topic?.authorId;
  }

  onPageChange(page: number): void {
    this.currentPage = page;
    window.scrollTo({ top: 0, behavior: 'smooth' });
    this.loadPosts();
  }

  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleString('en-US', {
      month: 'short',
      day: 'numeric',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  }

  getPageNumbers(): number[] {
    const pages: number[] = [];
    const maxVisible = 5;
    let start = Math.max(1, this.currentPage - Math.floor(maxVisible / 2));
    let end = Math.min(this.totalPages, start + maxVisible - 1);

    if (end - start < maxVisible - 1) {
      start = Math.max(1, end - maxVisible + 1);
    }

    for (let i = start; i <= end; i++) {
      pages.push(i);
    }

    return pages;
  }
}
