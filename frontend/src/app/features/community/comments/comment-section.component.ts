import { Component, Input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CommentService } from '../services';
import { Comment, CommentableType } from '../models';

@Component({
  selector: 'app-comment-section',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './comment-section.component.html',
  styleUrls: ['./comment-section.component.css'],
})
export class CommentSectionComponent implements OnInit {
  @Input() commentableType!: CommentableType;
  @Input() commentableId!: number;

  comments: Comment[] = [];
  loading = true;
  error: string | null = null;

  // Pagination
  currentPage = 1;
  totalPages = 1;
  totalComments = 0;
  pageSize = 20;

  // New comment
  newCommentContent = '';
  replyingToComment: Comment | null = null;
  submittingComment = false;

  // Edit
  editingComment: Comment | null = null;
  editContent = '';

  // Likes
  userLikedComments: Set<number> = new Set();

  // Current user (mock - should come from auth service)
  currentUserId: number | null = 1; // TODO: Get from auth service

  constructor(private commentService: CommentService) {}

  ngOnInit(): void {
    this.loadComments();
  }

  loadComments(): void {
    this.loading = true;
    this.error = null;

    this.commentService
      .getComments(
        this.commentableType,
        this.commentableId,
        this.currentPage,
        this.pageSize,
      )
      .subscribe({
        next: (response) => {
          this.comments = response.data;
          this.totalComments = response.total;
          this.totalPages = Math.ceil(response.total / this.pageSize);
          this.loading = false;
          this.checkUserLikes();
        },
        error: (err) => {
          this.error = 'Failed to load comments';
          this.loading = false;
          console.error('Error loading comments:', err);
        },
      });
  }

  checkUserLikes(): void {
    if (this.comments.length === 0 || !this.currentUserId) return;

    const commentIds = this.comments.map((c) => c.id);
    this.commentService.getUserLikes(commentIds).subscribe({
      next: (likedIds) => {
        this.userLikedComments = new Set(likedIds);
      },
    });
  }

  onSubmitComment(): void {
    if (!this.newCommentContent.trim() || this.submittingComment) return;

    this.submittingComment = true;

    const commentData = {
      content: this.newCommentContent,
      commentableType: this.commentableType,
      commentableId: this.commentableId,
      parentCommentId: this.replyingToComment?.id,
    };

    this.commentService.createComment(commentData).subscribe({
      next: () => {
        this.newCommentContent = '';
        this.replyingToComment = null;
        this.submittingComment = false;
        this.loadComments();
      },
      error: (err) => {
        alert('Failed to post comment. Please try again.');
        this.submittingComment = false;
        console.error('Error creating comment:', err);
      },
    });
  }

  onReply(comment: Comment): void {
    this.replyingToComment = comment;
    this.newCommentContent = '';
    setTimeout(() => {
      document.querySelector('.comment-input')?.scrollIntoView({ behavior: 'smooth' });
    }, 100);
  }

  cancelReply(): void {
    this.replyingToComment = null;
  }

  onEdit(comment: Comment): void {
    this.editingComment = comment;
    this.editContent = comment.content;
  }

  cancelEdit(): void {
    this.editingComment = null;
    this.editContent = '';
  }

  saveEdit(): void {
    if (!this.editingComment || !this.editContent.trim()) return;

    this.commentService
      .updateComment(this.editingComment.id, { content: this.editContent })
      .subscribe({
        next: () => {
          this.editingComment = null;
          this.editContent = '';
          this.loadComments();
        },
        error: (err) => {
          alert('Failed to update comment. Please try again.');
          console.error('Error updating comment:', err);
        },
      });
  }

  onDelete(comment: Comment): void {
    if (!confirm('Are you sure you want to delete this comment?')) return;

    this.commentService.deleteComment(comment.id).subscribe({
      next: () => {
        this.loadComments();
      },
      error: (err) => {
        alert('Failed to delete comment. Please try again.');
        console.error('Error deleting comment:', err);
      },
    });
  }

  toggleLike(comment: Comment): void {
    this.commentService.toggleLike(comment.id).subscribe({
      next: (response) => {
        if (response.liked) {
          this.userLikedComments.add(comment.id);
        } else {
          this.userLikedComments.delete(comment.id);
        }
        comment.likesCount = response.likesCount;
      },
      error: (err) => {
        console.error('Error toggling like:', err);
      },
    });
  }

  loadReplies(comment: Comment): void {
    // This would load replies for a specific comment
    // For simplicity, we're showing all comments in a flat structure
    // In a full implementation, you'd use the getReplies method
  }

  isAuthor(authorId: number): boolean {
    return this.currentUserId === authorId;
  }

  formatDate(dateString: string): string {
    const date = new Date(dateString);
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMs / 3600000);
    const diffDays = Math.floor(diffMs / 86400000);

    if (diffMins < 1) return 'Just now';
    if (diffMins < 60) return `${diffMins}m ago`;
    if (diffHours < 24) return `${diffHours}h ago`;
    if (diffDays < 7) return `${diffDays}d ago`;

    return date.toLocaleDateString('en-US', {
      month: 'short',
      day: 'numeric',
      year: date.getFullYear() !== now.getFullYear() ? 'numeric' : undefined,
    });
  }
}
