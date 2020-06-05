"""empty message

Revision ID: 7e250a6e02bc
Revises: 1f344536b903
Create Date: 2020-05-21 21:58:35.991805

"""
from alembic import op
import sqlalchemy as sa


# revision identifiers, used by Alembic.
revision = '7e250a6e02bc'
down_revision = '1f344536b903'
branch_labels = None
depends_on = None


def upgrade():
    # ### commands auto generated by Alembic - please adjust! ###
    op.create_table('table',
    sa.Column('id', sa.Integer(), nullable=False),
    sa.Column('databasename', sa.String(length=64), nullable=True),
    sa.PrimaryKeyConstraint('id')
    )
    op.create_index(op.f('ix_table_databasename'), 'table', ['databasename'], unique=True)
    # ### end Alembic commands ###


def downgrade():
    # ### commands auto generated by Alembic - please adjust! ###
    op.drop_index(op.f('ix_table_databasename'), table_name='table')
    op.drop_table('table')
    # ### end Alembic commands ###