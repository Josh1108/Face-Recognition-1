"""empty message

Revision ID: a444b2f02f30
Revises: 31b8437e82a1
Create Date: 2020-06-05 18:35:25.939631

"""
from alembic import op
import sqlalchemy as sa


# revision identifiers, used by Alembic.
revision = 'a444b2f02f30'
down_revision = '31b8437e82a1'
branch_labels = None
depends_on = None


def upgrade():
    # ### commands auto generated by Alembic - please adjust! ###
    op.create_table('examinee',
    sa.Column('id', sa.Integer(), nullable=False),
    sa.Column('databasename', sa.String(length=64), nullable=True),
    sa.Column('filename', sa.String(length=128), nullable=True),
    sa.Column('RollNumber', sa.String(length=64), nullable=True),
    sa.Column('attendance', sa.Boolean(), nullable=True),
    sa.PrimaryKeyConstraint('id')
    )
    op.create_index(op.f('ix_examinee_RollNumber'), 'examinee', ['RollNumber'], unique=True)
    op.create_index(op.f('ix_examinee_databasename'), 'examinee', ['databasename'], unique=True)
    op.create_table('user',
    sa.Column('id', sa.Integer(), nullable=False),
    sa.Column('username', sa.String(length=64), nullable=True),
    sa.Column('password_hash', sa.String(length=128), nullable=True),
    sa.PrimaryKeyConstraint('id')
    )
    op.create_index(op.f('ix_user_username'), 'user', ['username'], unique=True)
    # ### end Alembic commands ###


def downgrade():
    # ### commands auto generated by Alembic - please adjust! ###
    op.drop_index(op.f('ix_user_username'), table_name='user')
    op.drop_table('user')
    op.drop_index(op.f('ix_examinee_databasename'), table_name='examinee')
    op.drop_index(op.f('ix_examinee_RollNumber'), table_name='examinee')
    op.drop_table('examinee')
    # ### end Alembic commands ###