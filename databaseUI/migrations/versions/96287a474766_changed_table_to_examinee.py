"""changed table to examinee

Revision ID: 96287a474766
Revises: e538b20c6777
Create Date: 2020-05-25 03:20:10.008250

"""
from alembic import op
import sqlalchemy as sa


# revision identifiers, used by Alembic.
revision = '96287a474766'
down_revision = 'e538b20c6777'
branch_labels = None
depends_on = None


def upgrade():
    # ### commands auto generated by Alembic - please adjust! ###
    op.create_table('examinee',
    sa.Column('id', sa.Integer(), nullable=False),
    sa.Column('databasename', sa.String(length=64), nullable=True),
    sa.Column('image', sa.String(length=64), nullable=True),
    sa.Column('RollNumber', sa.String(length=64), nullable=True),
    sa.PrimaryKeyConstraint('id')
    )
    op.create_index(op.f('ix_examinee_RollNumber'), 'examinee', ['RollNumber'], unique=True)
    op.create_index(op.f('ix_examinee_databasename'), 'examinee', ['databasename'], unique=True)
    op.drop_index('ix_table_RollNumber', table_name='table')
    op.drop_index('ix_table_databasename', table_name='table')
    op.drop_table('table')
    # ### end Alembic commands ###


def downgrade():
    # ### commands auto generated by Alembic - please adjust! ###
    op.create_table('table',
    sa.Column('id', sa.INTEGER(), nullable=False),
    sa.Column('databasename', sa.VARCHAR(length=64), nullable=True),
    sa.Column('RollNumber', sa.VARCHAR(length=64), nullable=True),
    sa.Column('image', sa.BLOB(), nullable=True),
    sa.PrimaryKeyConstraint('id')
    )
    op.create_index('ix_table_databasename', 'table', ['databasename'], unique=1)
    op.create_index('ix_table_RollNumber', 'table', ['RollNumber'], unique=1)
    op.drop_index(op.f('ix_examinee_databasename'), table_name='examinee')
    op.drop_index(op.f('ix_examinee_RollNumber'), table_name='examinee')
    op.drop_table('examinee')
    # ### end Alembic commands ###
