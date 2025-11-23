import { ApiProperty } from '@nestjs/swagger';
import { User } from '../../users/entities/user.entity';

export class LoginResponseDto {
  @ApiProperty()
  user: User;

  @ApiProperty()
  token: string;
}
