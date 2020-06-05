"""empty message

Revision ID: 780abaa96cbf
Revises: 46bba187ab7b
Create Date: 2020-06-04 16:33:44.623520

"""
from alembic import op
import sqlalchemy as sa


# revision identifiers, used by Alembic.
revision = '780abaa96cbf'
down_revision = '46bba187ab7b'
branch_labels = None
depends_on = None


def upgrade():
    # ### commands auto generated by Alembic - please adjust! ###
    op.create_table('examinee',
    sa.Column('id', sa.Integer(), nullable=False),
    sa.Column('databasename', sa.String(length=64), nullable=True),
    sa.Column('name', sa.String(length=64), nullable=True),
    sa.Column('filename', sa.String(length=128), nullable=True),
    sa.Column('RollNumber', sa.String(length=64), nullable=True),
    sa.PrimaryKeyConstraint('id'),
    sa.UniqueConstraint('name')
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
